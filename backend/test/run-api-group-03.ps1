param(
    [string]$BaseUrl = "http://127.0.0.1:8080",
    [string]$MysqlUri = "",
    [string]$RedisHost = "192.168.1.170",
    [int]$RedisPort = 6379,
    [int]$RedisDb = 1
)

$ErrorActionPreference = "Stop"
$env:Path = [System.Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [System.Environment]::GetEnvironmentVariable('Path','User')
$RunId = Get-Date -Format "yyyyMMddHHmmss"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$ResultJson = Join-Path $Root "test/api-test-results-03-item-medical-$RunId.json"
$ResultMd = Join-Path $Root "test/api-test-results-03-item-medical-$RunId.md"
$Results = [System.Collections.Generic.List[object]]::new()

$ImgPetCover = Join-Path $Root "testImages/005d70677bbf3c352f7070c730f8686ad6d9314bb167a16b2f2b2588ad90fb13.jpg"
$ImgRescue = Join-Path $Root "testImages/0c526c6fef62bce303082cf2a0ffeaefe596e415cf460674841e24cf8d84f563.jpg"
$ImgLostPet = Join-Path $Root "testImages/0ddd22359d33d90a778e0019a99d352e01b20852c4d2a7c8b4c8c6fa94334d9e.jpg"

function Get-AppProperties {
    $properties = @{}
    Get-Content (Join-Path $Root "src/main/resources/application.properties") | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $properties[$parts[0].Trim()] = $parts[1].Trim()
        }
    }
    return $properties
}

function Get-MysqlUriFromProperties {
    $props = Get-AppProperties
    $jdbc = $props["spring.datasource.url"]
    if ($jdbc -notmatch '^jdbc:mysql://([^:/?]+):(\d+)/([^?]+)') {
        throw "Cannot parse spring.datasource.url from application.properties"
    }
    $user = $props["spring.datasource.username"]
    $password = $props["spring.datasource.password"]
    return "$user`:$password@$($matches[1]):$($matches[2])/$($matches[3])"
}

function New-RespCommand([string[]]$Parts) {
    $sb = [Text.StringBuilder]::new()
    [void]$sb.Append("*$($Parts.Count)`r`n")
    foreach ($part in $Parts) {
        $bytes = [Text.Encoding]::UTF8.GetByteCount($part)
        [void]$sb.Append("`$$bytes`r`n$part`r`n")
    }
    $sb.ToString()
}

function Invoke-RedisRaw([string[]]$Parts) {
    $client = [Net.Sockets.TcpClient]::new($RedisHost, $RedisPort)
    try {
        $stream = $client.GetStream()
        $payload = (New-RespCommand @("SELECT", "$RedisDb")) + (New-RespCommand $Parts)
        $bytes = [Text.Encoding]::UTF8.GetBytes($payload)
        $stream.Write($bytes, 0, $bytes.Length)
        Start-Sleep -Milliseconds 150
        $buffer = New-Object byte[] 4096
        $read = $stream.Read($buffer, 0, $buffer.Length)
        return [Text.Encoding]::UTF8.GetString($buffer, 0, $read)
    } finally {
        $client.Close()
    }
}

function Invoke-Redis([string[]]$Parts) {
    $text = Invoke-RedisRaw $Parts
    $lines = $text -split "`r?`n"
    if ($lines.Length -ge 4 -and $lines[-3] -match '^\$\d+') {
        return $lines[-2]
    }
    return $text.Trim()
}

function ConvertTo-SafeData($Value) {
    if ($null -eq $Value) { return $null }
    if ($Value -is [string] -or $Value.GetType().IsPrimitive) { return $Value }
    if ($Value -is [System.Collections.IEnumerable] -and -not ($Value -is [pscustomobject])) {
        return @($Value | ForEach-Object { ConvertTo-SafeData $_ })
    }

    $safe = [ordered]@{}
    foreach ($property in $Value.PSObject.Properties) {
        if ($property.Name -match '^(accessToken|refreshToken|password|code)$') {
            $safe[$property.Name] = "<redacted>"
        } else {
            $safe[$property.Name] = ConvertTo-SafeData $property.Value
        }
    }
    return [pscustomobject]$safe
}

function Invoke-Api {
    param(
        [string]$Id,
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [hashtable]$Headers = @{},
        [hashtable]$Form = $null,
        [hashtable]$Multipart = $null,
        [string]$Expected = "HTTP 200 / code 200"
    )
    $uri = "$BaseUrl$Path"
    $started = Get-Date
    try {
        $jsonFile = $null
        $curlArgs = @("-s", "-i", "-X", $Method)
        foreach ($header in $Headers.GetEnumerator()) {
            $curlArgs += @("-H", "$($header.Key): $($header.Value)")
        }
        if ($null -ne $Multipart) {
            foreach ($entry in $Multipart.GetEnumerator()) {
                if ($entry.Value -is [IO.FileInfo]) {
                    $curlArgs += @("-F", "$($entry.Key)=@$($entry.Value.FullName)")
                } else {
                    $curlArgs += @("-F", "$($entry.Key)=$($entry.Value)")
                }
            }
        } elseif ($null -ne $Form) {
            $formBody = ($Form.GetEnumerator() | ForEach-Object {
                "$([uri]::EscapeDataString($_.Key))=$([uri]::EscapeDataString([string]$_.Value))"
            }) -join "&"
            $curlArgs += @("-H", "Content-Type: application/x-www-form-urlencoded", "--data", $formBody)
        } elseif ($null -ne $Body) {
            $jsonFile = Join-Path ([IO.Path]::GetTempPath()) "api-json-$([Guid]::NewGuid().ToString('N')).json"
            ($Body | ConvertTo-Json -Depth 30 -Compress) | Set-Content -Path $jsonFile -Encoding UTF8
            $curlArgs += @("-H", "Content-Type: application/json", "--data-binary", "@$jsonFile")
        }
        $curlArgs += $uri
        $raw = & curl.exe @curlArgs 2>&1
        if ($jsonFile -and (Test-Path $jsonFile)) {
            Remove-Item -LiteralPath $jsonFile -Force
        }

        $statusLine = $raw | Where-Object { $_ -match '^HTTP/' } | Select-Object -Last 1
        $statusCode = if ($statusLine -match 'HTTP/\S+\s+(\d+)') { [int]$matches[1] } else { $null }
        if ($null -eq $statusCode) {
            throw "curl returned no HTTP response: $($raw -join ' ')"
        }
        $blankIndexes = for ($i = 0; $i -lt $raw.Length; $i++) { if ($raw[$i] -eq "") { $i } }
        $splitIndex = if ($blankIndexes.Count -gt 0) { $blankIndexes[-1] } else { -1 }
        $bodyLines = if ($splitIndex -ge 0 -and $splitIndex -lt ($raw.Length - 1)) { $raw[($splitIndex + 1)..($raw.Length - 1)] } else { @() }
        $content = ($bodyLines -join "`n")
        $parsed = $null
        if ($content.TrimStart().StartsWith("{")) {
            $parsed = $content | ConvertFrom-Json
        }

        $expectedStatus = if ($Expected -match 'HTTP\s+(\d+)') { [int]$matches[1] } else { 200 }
        $expectedCode = if ($Expected -match 'code\s+(\d+)') { [int]$matches[1] } else { $null }
        $ok = if ($null -ne $expectedCode -and $parsed) {
            $statusCode -eq $expectedStatus -and $parsed.code -eq $expectedCode
        } elseif ($parsed -and $statusCode -eq 200) {
            $parsed.code -eq 200
        } else {
            $statusCode -eq $expectedStatus
        }

        $result = [ordered]@{
            id = $Id
            method = $Method
            path = $Path
            status = $statusCode
            code = if ($parsed) { [int]$parsed.code } else { $null }
            expected = $Expected
            passed = $ok
            contentType = ""
            elapsedMs = [int]((Get-Date) - $started).TotalMilliseconds
            data = if ($parsed) { $parsed.data } else { "content length=$($content.Length)" }
            message = if ($parsed) { $parsed.message } else { "" }
        }
        $Results.Add([pscustomobject]$result)
        return [pscustomobject]$result
    } catch {
        $status = $null
        $body = $_.Exception.Message
        $parsed = $null
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            try {
                if ($_.ErrorDetails.Message) {
                    $body = $_.ErrorDetails.Message
                    if ($body.TrimStart().StartsWith("{")) {
                        $parsed = $body | ConvertFrom-Json
                    }
                }
            } catch {}
        }
        $expectedStatus = if ($Expected -match 'HTTP\s+(\d+)') { [int]$matches[1] } else { 200 }
        $expectedCode = if ($Expected -match 'code\s+(\d+)') { [int]$matches[1] } else { $null }
        $ok = if ($null -ne $expectedCode -and $parsed) {
            $status -eq $expectedStatus -and $parsed.code -eq $expectedCode
        } else {
            $status -eq $expectedStatus
        }
        $result = [ordered]@{
            id = $Id
            method = $Method
            path = $Path
            status = $status
            code = if ($parsed) { [int]$parsed.code } else { $null }
            expected = $Expected
            passed = $ok
            contentType = ""
            elapsedMs = [int]((Get-Date) - $started).TotalMilliseconds
            data = $null
            message = if ($parsed) { $parsed.message } else { $body }
        }
        $Results.Add([pscustomobject]$result)
        return [pscustomobject]$result
    }
}

function Assert-ApiPreflight {
    $probe = Invoke-Api -Id "PRE-01" -Method "GET" -Path "/auth/check/username/api_probe_$RunId" -Expected "HTTP 200 / code 200"
    if (-not $probe.passed) {
        throw "API preflight failed: status=$($probe.status) code=$($probe.code) message=$($probe.message)"
    }
}

function Mysql-Exec([string]$Sql) {
    if (-not $MysqlUri) {
        $script:MysqlUri = Get-MysqlUriFromProperties
    }
    & "C:/Dev/tools/mysql-shell-9.7.0-windows-x86-64bit/bin/mysqlsh.exe" --sql --uri $MysqlUri -e $Sql | Out-Null
}

function Send-CodeAndRead([string]$Email) {
    Invoke-Api -Id "AUTH-send-$Email" -Method "POST" -Path "/auth/check/code?email=$([uri]::EscapeDataString($Email))" | Out-Null
    $key = "pet_adoption:user.forgetpwd.code.$Email"
    return Invoke-Redis @("GET", $key)
}

function Register-TestUser([string]$Username, [string]$Email, [string]$Password, [string]$Phone) {
    $code = Send-CodeAndRead $Email
    $form = @{
        username = $Username
        password = $Password
        email = $Email
        phone = $Phone
        code = $code
    }
    return Invoke-Api -Id "AUTH-register-$Username" -Method "POST" -Path "/auth/register" -Form $form
}

function Login-TestUser([string]$Username, [string]$Password) {
    return Invoke-Api -Id "AUTH-login-$Username" -Method "POST" -Path "/auth/login" -Body @{
        username = $Username
        password = $Password
    }
}

function Mysql-Scalar([string]$Sql) {
    if (-not $MysqlUri) {
        $script:MysqlUri = Get-MysqlUriFromProperties
    }
    $raw = & "C:/Dev/tools/mysql-shell-9.7.0-windows-x86-64bit/bin/mysqlsh.exe" --sql --uri $MysqlUri -e $Sql
    $lines = @($raw | Where-Object {
        $_ -and
        -not $_.StartsWith("WARNING:") -and
        -not $_.StartsWith("Please provide the password")
    })
    if (-not $lines -or $lines.Count -lt 2) { return $null }
    $valueLine = $lines[-1].Trim()
    if (-not $valueLine) { return $null }
    return ($valueLine -split "`t")[-1].Trim()
}

function Require-MysqlScalar([string]$Sql, [string]$Name, [int]$RetryCount = 5, [int]$DelayMs = 300) {
    for ($i = 0; $i -lt $RetryCount; $i++) {
        $value = Mysql-Scalar $Sql
        if ($null -ne $value -and "$value".Trim() -ne "") {
            return $value
        }
        Start-Sleep -Milliseconds $DelayMs
    }
    throw "Failed to fetch mysql scalar for $Name"
}

$suffix = Get-Date -Format "HHmmss"
$password = "Api@123456"
$worker = "api3_worker_$suffix"
$doctor = "api3_doctor_$suffix"
$donor = "api3_donor_$suffix"
$reader = "api3_reader_$suffix"

Assert-ApiPreflight

Register-TestUser $worker "$worker@example.com" $password "13811000001" | Out-Null
Register-TestUser $doctor "$doctor@example.com" $password "13811000002" | Out-Null
Register-TestUser $donor "$donor@example.com" $password "13811000003" | Out-Null
Register-TestUser $reader "$reader@example.com" $password "13811000004" | Out-Null

Mysql-Exec "UPDATE user SET role = CASE username WHEN '$worker' THEN 3 WHEN '$doctor' THEN 8 ELSE role END WHERE username IN ('$worker','$doctor');"

$workerLogin = Login-TestUser $worker $password
$doctorLogin = Login-TestUser $doctor $password
$donorLogin = Login-TestUser $donor $password
$readerLogin = Login-TestUser $reader $password

$workerHeaders = @{ Authorization = "Bearer $($workerLogin.data.accessToken)" }
$doctorHeaders = @{ Authorization = "Bearer $($doctorLogin.data.accessToken)" }
$donorHeaders = @{ Authorization = "Bearer $($donorLogin.data.accessToken)" }
$readerHeaders = @{ Authorization = "Bearer $($readerLogin.data.accessToken)" }

$doctorId = $doctorLogin.data.id
$donorId = $donorLogin.data.id

$future = (Get-Date).AddDays(30)
$future2 = (Get-Date).AddDays(45)
$future3 = (Get-Date).AddDays(60)
$future4 = (Get-Date).AddDays(90)
$past = (Get-Date).AddDays(-3)

$petAdd = Invoke-Api -Id "PRE-PET-01" -Method "POST" -Path "/pets" -Headers $workerHeaders -Body @{
    name = "Medical Pet $suffix"
    age = 12
    sex = "FEMALE"
    type = "CAT"
    breed = "Domestic Short Hair"
    description = "API group 03 medical pet"
    health = "GOOD"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Shelter medical room"
}
$petId = $petAdd.data.id
Invoke-Api -Id "PRE-PET-02" -Method "POST" -Path "/pets/$petId/media" -Headers $workerHeaders -Multipart @{ file = Get-Item $ImgPetCover; name = "medical-cover"; description = "medical cover"; isCover = "true" } | Out-Null
Invoke-Api -Id "PRE-PET-03" -Method "PUT" -Path "/pets/$petId/status" -Headers $workerHeaders -Body @{ petId = $petId; status = "SHELTERED"; reason = "Ready for testing" } | Out-Null

$categoryAdd = Invoke-Api -Id "ITEM-01" -Method "POST" -Path "/items/categories" -Headers $workerHeaders -Body @{ name = "API-CAT-$suffix"; description = "Primary category" }
$categoryId = $categoryAdd.data.id
Invoke-Api -Id "ITEM-02" -Method "PUT" -Path "/items/categories/$categoryId" -Headers $workerHeaders -Body @{ name = "API-CAT-UPD-$suffix"; description = "Updated category" } | Out-Null
Invoke-Api -Id "ITEM-03" -Method "GET" -Path "/items/categories/$categoryId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ITEM-04" -Method "GET" -Path "/items/categories?name=API-CAT&page=1&size=10" -Headers $workerHeaders | Out-Null
$emptyCategory = Invoke-Api -Id "ITEM-10-pre" -Method "POST" -Path "/items/categories" -Headers $workerHeaders -Body @{ name = "API-EMPTY-$suffix"; description = "Empty category" }
$emptyCategoryId = $emptyCategory.data.id

$itemAdd = Invoke-Api -Id "ITEM-05" -Method "POST" -Path "/items/items" -Headers $workerHeaders -Body @{ name = "API Item $suffix"; categoryId = $categoryId; description = "General stock item"; unit = "bag" }
$itemId = $itemAdd.data.id
Invoke-Api -Id "ITEM-06" -Method "PUT" -Path "/items/items/$itemId" -Headers $workerHeaders -Body @{ name = "API Item Updated $suffix"; categoryId = $categoryId; description = "Updated stock item"; unit = "box" } | Out-Null
Invoke-Api -Id "ITEM-07" -Method "GET" -Path "/items/items/$itemId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ITEM-08" -Method "GET" -Path "/items/items?category=$categoryId&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ITEM-09-unauthorized" -Method "DELETE" -Path "/items/items/$itemId" -Headers $donorHeaders -Expected "HTTP 403 / code 403" | Out-Null
Invoke-Api -Id "ITEM-10-not-empty" -Method "DELETE" -Path "/items/categories/$categoryId" -Headers $workerHeaders -Expected "HTTP 409 / code 409" | Out-Null
$extraItem = Invoke-Api -Id "ITEM-09-pre" -Method "POST" -Path "/items/items" -Headers $workerHeaders -Body @{ name = "Discard Item $suffix"; categoryId = $categoryId; description = "Disposable item"; unit = "piece" }
Invoke-Api -Id "ITEM-09" -Method "DELETE" -Path "/items/items/$($extraItem.data.id)" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ITEM-10" -Method "DELETE" -Path "/items/categories/$emptyCategoryId" -Headers $workerHeaders | Out-Null
$vaccineItem = Invoke-Api -Id "MED-seed-item-vaccine" -Method "POST" -Path "/items/items" -Headers $workerHeaders -Body @{ name = "API Vaccine Item $suffix"; categoryId = $categoryId; description = "Seed vaccine item"; unit = "dose" }
$dewormerItem = Invoke-Api -Id "MED-seed-item-deworm" -Method "POST" -Path "/items/items" -Headers $workerHeaders -Body @{ name = "API Dewormer Item $suffix"; categoryId = $categoryId; description = "Seed deworm item"; unit = "pill" }
$orderItem = Invoke-Api -Id "MED-seed-item-order" -Method "POST" -Path "/items/items" -Headers $workerHeaders -Body @{ name = "API Order Item $suffix"; categoryId = $categoryId; description = "Treatment order item"; unit = "tablet" }
Mysql-Exec "INSERT INTO vaccine (item_id, illness, min_age, times, create_time) VALUES ($($vaccineItem.data.id), 'Cat Flu $suffix', 1, 3, NOW());"
Mysql-Exec "INSERT INTO dewormer (item_id, type, min_age, times, create_time) VALUES ($($dewormerItem.data.id), 'OTHER', 1, 2, NOW());"
$vaccineId = [long](Require-MysqlScalar "select cast(id as char) as value from vaccine where item_id = $($vaccineItem.data.id) order by id desc limit 1;" "vaccineId")
$dewormerId = [long](Require-MysqlScalar "select cast(id as char) as value from dewormer where item_id = $($dewormerItem.data.id) order by id desc limit 1;" "dewormerId")
$orderItemId = $orderItem.data.id

$donationBegin = Invoke-Api -Id "DON-01" -Method "PUT" -Path "/items/donations" -Headers $donorHeaders
$donationUuid = $donationBegin.data
$upload1 = Invoke-Api -Id "DON-02" -Method "POST" -Path "/items/donations/upload/$donationUuid" -Headers $donorHeaders -Multipart @{ file = Get-Item $ImgRescue }
if ($upload1.data) { Invoke-Api -Id "DON-03" -Method "DELETE" -Path "/items/donations/upload/$donationUuid/$($upload1.data)" -Headers $donorHeaders | Out-Null }
Start-Sleep -Milliseconds 1000
Invoke-Api -Id "DON-03-reupload" -Method "POST" -Path "/items/donations/upload/$donationUuid" -Headers $donorHeaders -Multipart @{ file = Get-Item $ImgRescue } | Out-Null
$donationAdd = Invoke-Api -Id "DON-04" -Method "POST" -Path "/items/donations" -Headers $donorHeaders -Body @{ uuid = $donationUuid; delivery = "ADDRESS"; address = "Donation room $suffix"; description = "Initial donation"; items = @(@{ name = "existing"; itemId = $itemId; categoryId = $categoryId; description = "Stock item donation"; count = "5"; expireTime = $future2.ToString("o") }) }
$donationId = $donationAdd.data.id
Invoke-Api -Id "DON-05" -Method "PUT" -Path "/items/donations/$donationId" -Headers $donorHeaders -Body @{ delivery = "EXPRESS"; trackingNumber = "EXP-$suffix"; description = "Updated donation"; items = @(@{ name = "existing"; itemId = $itemId; categoryId = $categoryId; description = "Updated item donation"; count = "6"; expireTime = $future3.ToString("o") }) } | Out-Null
Invoke-Api -Id "DON-06-1" -Method "PATCH" -Path "/items/donations/$donationId/status" -Headers $workerHeaders -Body @{ status = "PENDING"; reason = "Accepted" } | Out-Null
Invoke-Api -Id "DON-06-2" -Method "PATCH" -Path "/items/donations/$donationId/status" -Headers $workerHeaders -Body @{ status = "TRANSFERRING"; reason = "On the way" } | Out-Null
Invoke-Api -Id "DON-06-3" -Method "PATCH" -Path "/items/donations/$donationId/status" -Headers $workerHeaders -Body @{ status = "RECEIVED"; reason = "Received" } | Out-Null
Invoke-Api -Id "DON-06-4" -Method "PATCH" -Path "/items/donations/$donationId/status" -Headers $workerHeaders -Body @{ status = "STOCKED"; reason = "Stocked" } | Out-Null
Invoke-Api -Id "DON-07" -Method "PATCH" -Path "/items/donations/$donationId/status" -Headers $workerHeaders -Body @{ status = "PENDING"; reason = "rollback" } -Expected "HTTP 422 / code 422" | Out-Null
$donationBegin2 = Invoke-Api -Id "DON-08-pre-begin" -Method "PUT" -Path "/items/donations" -Headers $donorHeaders
Invoke-Api -Id "DON-08-pre-upload" -Method "POST" -Path "/items/donations/upload/$($donationBegin2.data)" -Headers $donorHeaders -Multipart @{ file = Get-Item $ImgRescue } | Out-Null
$donation2 = Invoke-Api -Id "DON-08-pre-add" -Method "POST" -Path "/items/donations" -Headers $donorHeaders -Body @{ uuid = $donationBegin2.data; delivery = "OTHER"; description = "Backing flow donation"; items = @(@{ name = "temp"; itemId = $itemId; categoryId = $categoryId; description = "Backing item"; count = "2"; expireTime = $future2.ToString("o") }) }
$donation2Id = $donation2.data.id
Invoke-Api -Id "DON-08-pre-pending" -Method "PATCH" -Path "/items/donations/$donation2Id/status" -Headers $workerHeaders -Body @{ status = "PENDING"; reason = "Pending" } | Out-Null
Invoke-Api -Id "DON-08-pre-refused" -Method "PATCH" -Path "/items/donations/$donation2Id/status" -Headers $workerHeaders -Body @{ status = "REFUSED"; reason = "Cannot receive" } | Out-Null
Invoke-Api -Id "DON-08-pre-backing" -Method "PATCH" -Path "/items/donations/$donation2Id/status" -Headers $workerHeaders -Body @{ status = "BACKING"; reason = "Return to donor" } | Out-Null
Invoke-Api -Id "DON-08" -Method "PATCH" -Path "/items/donations/$donation2Id/status" -Headers $donorHeaders -Body @{ status = "CLOSED"; reason = "Closed by donor" } | Out-Null
Invoke-Api -Id "DON-09" -Method "GET" -Path "/items/donations/$donationId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "DON-10" -Method "GET" -Path "/items/donations?user=$donorId&page=1&size=10" -Headers $workerHeaders | Out-Null

$stockIn = Invoke-Api -Id "ITEM-11" -Method "POST" -Path "/items/stocks" -Headers $workerHeaders -Body @{ itemId = $itemId; count = "10"; action = "IN"; sourceType = "PURCHASE"; purpose = "Initial purchase"; expireTime = $future4.ToString("o"); price = "12.50"; totalPrice = "125.00" }
$stockId = $stockIn.data.id
Invoke-Api -Id "ITEM-12" -Method "POST" -Path "/items/stocks" -Headers $doctorHeaders -Body @{ id = $stockId; itemId = $itemId; count = "3"; action = "OUT"; sourceType = "PURCHASE"; purpose = "Medical use" } | Out-Null
Invoke-Api -Id "ITEM-13" -Method "POST" -Path "/items/stocks" -Headers $workerHeaders -Body @{ id = $stockId; itemId = $itemId; count = "2"; action = "DESTROY"; sourceType = "PURCHASE"; purpose = "Broken packages" } | Out-Null
Invoke-Api -Id "ITEM-13-insufficient" -Method "POST" -Path "/items/stocks" -Headers $workerHeaders -Body @{ id = $stockId; itemId = $itemId; count = "100"; action = "OUT"; sourceType = "PURCHASE"; purpose = "Too much" } -Expected "HTTP 422 / code 422" | Out-Null
Invoke-Api -Id "ITEM-14" -Method "GET" -Path "/items/stocks/$stockId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ITEM-15-worker" -Method "GET" -Path "/items/stocks?item=$itemId&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ITEM-15-user" -Method "GET" -Path "/items/stocks?page=1&size=10" -Headers $donorHeaders | Out-Null
Invoke-Api -Id "ITEM-16" -Method "GET" -Path "/items/records?stock=$stockId&page=1&size=10" -Headers $workerHeaders | Out-Null
$subscribeAdd = Invoke-Api -Id "ITEM-17" -Method "POST" -Path "/items/subscribe" -Headers $donorHeaders -Body @{ elementId = $itemId; action = "ITEM_COUNT"; count = "3" }
$subscribeId = $subscribeAdd.data.id
Invoke-Api -Id "ITEM-18" -Method "GET" -Path "/items/subscribe/$subscribeId" -Headers $donorHeaders | Out-Null
Invoke-Api -Id "ITEM-19" -Method "GET" -Path "/items/subscribe?element=$itemId&action=ITEM_COUNT&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ITEM-20" -Method "DELETE" -Path "/items/subscribe?subscribeIds=$subscribeId" -Headers $donorHeaders | Out-Null
$registrationAdd = Invoke-Api -Id "MED-01" -Method "POST" -Path "/medical/first" -Headers $doctorHeaders -Body @{ petId = $petId; name = "Medical Pet $suffix"; age = 12; weight = 4.5; temperature = 38.5; description = "Initial first registration"; immunities = @(@{ medicine = "Triple Vaccine"; illness = "Panleukopenia"; count = 1; total = 3; immunityTime = $past.ToString("o") }); allergies = @(@{ source = "Seafood"; discoveryTime = $past.ToString("o") }) }
$registrationId = $registrationAdd.data.id
Invoke-Api -Id "MED-02" -Method "GET" -Path "/medical/first?pet=$petId&page=1&size=10" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-03" -Method "GET" -Path "/medical/first/$registrationId" -Headers $doctorHeaders | Out-Null
$recordAdd = Invoke-Api -Id "MED-04" -Method "POST" -Path "/medical/record/pet/$petId" -Headers $doctorHeaders -Body @{ petAge = 12; ownerId = $donorId; ownerPhone = "13811000003"; price = "88.00"; cost = "20.00"; type = "FIRST" }
$recordId = $recordAdd.data.id
Invoke-Api -Id "MED-05" -Method "PUT" -Path "/medical/record/$recordId" -Headers $doctorHeaders -Body @{ ownerId = $donorId; ownerPhone = "13811000999"; status = "PROCESSING"; type = "REVISIT"; startTime = (Get-Date).AddHours(-1).ToString("o"); endTime = (Get-Date).ToString("o"); cost = "35.00" } | Out-Null
Invoke-Api -Id "MED-06" -Method "GET" -Path "/medical/record/$recordId" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-07" -Method "GET" -Path "/medical/record?pet=$petId&page=1&size=10" -Headers $doctorHeaders | Out-Null
$detailAdd = Invoke-Api -Id "MED-08" -Method "POST" -Path "/medical/detail" -Headers $doctorHeaders -Body @{ recordId = $recordId; summary = "Respiratory issue"; description = "Mild cough and low appetite"; history = "2 days cough"; pastHistory = "No major history"; lifeHabit = "Indoor foster"; weight = 4.4; temperature = 38.7; heartRate = 120; respiratoryRate = 26; physicalExam = "Lungs slightly noisy" }
$detailId = $detailAdd.data.id
Invoke-Api -Id "MED-09" -Method "GET" -Path "/medical/detail/$detailId" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-10" -Method "GET" -Path "/medical/detail?record=$recordId&page=1&size=10" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-11" -Method "PUT" -Path "/medical/detail/$detailId" -Headers $doctorHeaders -Body @{ doctorId = $doctorId; isCompleted = $false; summary = "Respiratory issue updated"; physicalExam = "Exam updated"; diagnosis = "Pending"; differential = "URI"; exam = "Blood + xray"; treatment = "Supportive care"; advice = "Observe appetite" } | Out-Null
$examBegin = Invoke-Api -Id "MED-17" -Method "PUT" -Path "/medical/details/$detailId/exam" -Headers $doctorHeaders
$examUuid = $examBegin.data
$examUpload = Invoke-Api -Id "MED-18" -Method "PUT" -Path "/medical/exam/$examUuid/doc" -Headers $doctorHeaders -Multipart @{ file = Get-Item $ImgRescue; name = "blood-report" }
if ($examUpload.data) { Invoke-Api -Id "MED-19" -Method "DELETE" -Path "/medical/exam/$examUuid/doc/$($examUpload.data)" -Headers $doctorHeaders | Out-Null }
Start-Sleep -Milliseconds 1000
Invoke-Api -Id "MED-19-reupload" -Method "PUT" -Path "/medical/exam/$examUuid/doc" -Headers $doctorHeaders -Multipart @{ file = Get-Item $ImgRescue; name = "blood-report-final" } | Out-Null
$examAdd = Invoke-Api -Id "MED-20" -Method "POST" -Path "/medical/exam/$examUuid" -Headers $doctorHeaders -Body @{ name = "Blood Test"; text = "Slight inflammation"; examType = "BLOOD"; checkTime = (Get-Date).ToString("o") }
$examId = $examAdd.data.id
Invoke-Api -Id "MED-21" -Method "GET" -Path "/medical/exam/$examId" -Headers $doctorHeaders | Out-Null
$examAsset = @($examAdd.data.files)[0].assetUrl
if ($examAsset) { Invoke-Api -Id "MED-21-asset" -Method "GET" -Path $examAsset -Expected "HTTP 200 exam asset" | Out-Null }
$diagnosisAdd = Invoke-Api -Id "MED-13" -Method "POST" -Path "/medical/detail/$detailId/diagnosis" -Headers $doctorHeaders -Body @{ result = "Upper respiratory infection"; examinations = @($examId) }
Invoke-Api -Id "MED-14" -Method "DELETE" -Path "/medical/diagnosis/$($diagnosisAdd.data.id)" -Headers $doctorHeaders | Out-Null
$planAdd = Invoke-Api -Id "MED-15" -Method "POST" -Path "/medical/detail/$detailId/plan" -Headers $doctorHeaders -Body @{ plan = "Supportive medicine for 5 days"; startTime = (Get-Date).ToString("o"); endTime = $future.ToString("o"); orders = @(@{ itemId = $orderItemId; type = "MEDICINE"; count = "5"; unit = "tablet"; price = "2.50" }) }
Invoke-Api -Id "MED-16" -Method "DELETE" -Path "/medical/plan" -Headers $doctorHeaders -Body @{ ids = @($planAdd.data.id) } | Out-Null
Invoke-Api -Id "MED-12" -Method "PATCH" -Path "/medical/detail/$detailId" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-17-closed" -Method "PUT" -Path "/medical/details/$detailId/exam" -Headers $doctorHeaders -Expected "HTTP 422 / code 422" | Out-Null
Invoke-Api -Id "MED-22" -Method "POST" -Path "/medical/vaccine/pet/$petId" -Headers $doctorHeaders -Body @{ vaccineId = $vaccineId; petAge = 12; times = 1 } | Out-Null
Invoke-Api -Id "MED-23" -Method "GET" -Path "/medical/vaccine/pet/$petId" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-24" -Method "GET" -Path "/medical/vaccine/pet/$petId/new" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-25" -Method "POST" -Path "/medical/deworm/pet/$petId" -Headers $workerHeaders -Body @{ dewormerId = $dewormerId; times = 1 } | Out-Null
Invoke-Api -Id "MED-26" -Method "GET" -Path "/medical/deworm/pet/$petId" -Headers $doctorHeaders | Out-Null
$rehabAdd = Invoke-Api -Id "MED-27" -Method "POST" -Path "/medical/rehab/pet/$petId" -Headers $doctorHeaders -Body @{ age = 12; title = "Rehab plan $suffix"; content = "Daily gentle exercise"; frequency = "Daily"; type = "PHYSICAL"; startTime = (Get-Date).ToString("o"); endTime = $future4.ToString("o"); orders = @(@{ itemId = $orderItemId; type = "OTHER"; count = "1"; unit = "session"; price = "0" }) }
$rehabId = $rehabAdd.data.id
Invoke-Api -Id "MED-28" -Method "GET" -Path "/medical/rehab/$rehabId" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-29" -Method "GET" -Path "/medical/rehab?pet=$petId&page=1&size=10" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-30" -Method "PUT" -Path "/medical/rehab/$rehabId/status" -Headers $doctorHeaders -Body @{ status = "COMPLETED"; reason = "Recovered well" } | Out-Null
$rehabRecord = Invoke-Api -Id "MED-31" -Method "POST" -Path "/medical/rehab/$rehabId/record" -Headers $doctorHeaders -Multipart @{ step = "Step 1"; reaction = "Cooperative"; note = "Good progress"; files = Get-Item $ImgLostPet }
Invoke-Api -Id "MED-32" -Method "GET" -Path "/medical/rehab/$rehabId/record" -Headers $doctorHeaders | Out-Null
$rehabAsset = @($rehabRecord.data.files)[0].assetUrl
if ($rehabAsset) { Invoke-Api -Id "MED-32-asset" -Method "GET" -Path $rehabAsset -Expected "HTTP 200 rehab asset" | Out-Null }
$healthAdd = Invoke-Api -Id "MED-33" -Method "POST" -Path "/medical/health/pet/$petId" -Headers $doctorHeaders -Body @{ age = 12; weight = 4.6; scoreBcs = 88; scoreMental = 90; scoreAppetite = 84; summary = "Healthy overall" }
Invoke-Api -Id "MED-34" -Method "GET" -Path "/medical/health/$($healthAdd.data.id)" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-35" -Method "GET" -Path "/medical/health?page=1&size=10" -Headers $doctorHeaders | Out-Null
Invoke-Api -Id "MED-36" -Method "GET" -Path "/medical/health/pet/${petId}?page=1&size=10" -Headers $doctorHeaders | Out-Null
$SafeResults = $Results | ForEach-Object {
    [pscustomobject][ordered]@{
        id = $_.id
        method = $_.method
        path = $_.path
        status = $_.status
        code = $_.code
        expected = $_.expected
        passed = $_.passed
        contentType = $_.contentType
        elapsedMs = $_.elapsedMs
        data = ConvertTo-SafeData $_.data
        message = $_.message
    }
}
$SafeResults | ConvertTo-Json -Depth 50 | Set-Content -Path $ResultJson -Encoding UTF8

$passed = ($Results | Where-Object passed).Count
$failed = $Results.Count - $passed
$lines = @()
$lines += "# API Test Results 03: Item Donation and Medical"
$lines += ""
$lines += "- Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$lines += "- BaseUrl: $BaseUrl"
$lines += "- Summary: passed=$passed failed=$failed total=$($Results.Count)"
$lines += "- Raw JSON: $(Split-Path -Leaf $ResultJson)"
$lines += ""
$lines += "| ID | Method | Path | HTTP | code | Result | Message |"
$lines += "|---|---|---|---:|---:|---|---|"
foreach ($r in $Results) {
    $flag = if ($r.passed) { "PASS" } else { "FAIL" }
    $msg = ($r.message -replace "`r?`n", " ") -replace "\|", "/"
    $lines += "| $($r.id) | $($r.method) | $($r.path) | $($r.status) | $($r.code) | $flag | $msg |"
}
$lines | Set-Content -Path $ResultMd -Encoding UTF8

Write-Output "RESULT_MD=$ResultMd"
Write-Output "RESULT_JSON=$ResultJson"
Write-Output "PASSED=$passed FAILED=$failed TOTAL=$($Results.Count)"
