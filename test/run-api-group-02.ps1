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
$ResultJson = Join-Path $Root "test/api-test-results-02-pet-rescue-lost-publicity-$RunId.json"
$ResultMd = Join-Path $Root "test/api-test-results-02-pet-rescue-lost-publicity-$RunId.md"
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

$suffix = Get-Date -Format "HHmmss"
$password = "Api@123456"
$worker = "api2_worker_$suffix"
$owner = "api2_owner_$suffix"
$other = "api2_other_$suffix"

Assert-ApiPreflight

$workerReg = Register-TestUser $worker "$worker@example.com" $password "13810000001"
$ownerReg = Register-TestUser $owner "$owner@example.com" $password "13810000002"
$otherReg = Register-TestUser $other "$other@example.com" $password "13810000003"

Mysql-Exec "UPDATE user SET role = CASE username WHEN '$worker' THEN 3 ELSE role END WHERE username IN ('$worker');"

$workerLogin = Login-TestUser $worker $password
$ownerLogin = Login-TestUser $owner $password
$otherLogin = Login-TestUser $other $password

$workerHeaders = @{ Authorization = "Bearer $($workerLogin.data.accessToken)" }
$ownerHeaders = @{ Authorization = "Bearer $($ownerLogin.data.accessToken)" }
$otherHeaders = @{ Authorization = "Bearer $($otherLogin.data.accessToken)" }

$workerId = $workerLogin.data.id
$ownerId = $ownerLogin.data.id
$otherId = $otherLogin.data.id

# PetController
$petAdd = Invoke-Api -Id "PET-01" -Method "POST" -Path "/pets" -Headers $workerHeaders -Body @{
    name = "API Pet $suffix"
    age = 8
    sex = "FEMALE"
    type = "CAT"
    breed = "Domestic Short Hair"
    description = "Created by API group 02"
    health = "GOOD"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Pet Street 1"
}
$petId = $petAdd.data.id

Invoke-Api -Id "PET-02" -Method "POST" -Path "/pets/$petId/location" -Headers $workerHeaders -Body @{
    province = "Guangdong"
    city = "Shenzhen"
    district = "Futian"
    detailAddress = "Second shelter address"
} | Out-Null

Invoke-Api -Id "PET-03" -Method "GET" -Path "/pets?status=WAITING&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "PET-04" -Method "GET" -Path "/pets/$petId" -Headers $workerHeaders | Out-Null

Invoke-Api -Id "PET-05" -Method "PUT" -Path "/pets/$petId" -Headers $workerHeaders -Body @{
    name = "API Pet Updated $suffix"
    age = 10
    sex = "FEMALE"
    type = "CAT"
    breed = "British Shorthair"
    health = "RECOVERING"
    description = "Updated by API group 02"
} | Out-Null

$petMedia = Invoke-Api -Id "PET-06" -Method "POST" -Path "/pets/$petId/media" -Headers $workerHeaders -Multipart @{
    file = Get-Item $ImgPetCover
    name = "Pet cover"
    description = "Main cover"
    isCover = "true"
}
$petMediaId = $petMedia.data.id
$petAssetUrl = $petMedia.data.assetUrl
if ($petAssetUrl) {
    Invoke-Api -Id "PET-06-asset" -Method "GET" -Path $petAssetUrl -Expected "HTTP 200 pet asset" | Out-Null
}

Invoke-Api -Id "PET-07" -Method "PUT" -Path "/pets/$petId/media/$petMediaId" -Headers $workerHeaders -Body @{
    name = "Pet cover updated"
    description = "Updated description"
    isCover = $true
} | Out-Null

$petTagAdd = Invoke-Api -Id "PET-09" -Method "POST" -Path "/pets/$petId/tags" -Headers $workerHeaders -Body @{
    tags = @("friendly", "dewormed")
}
$tagIds = @($petTagAdd.data | ForEach-Object { $_.id })
if ($tagIds.Count -gt 0) {
    Invoke-Api -Id "PET-10" -Method "DELETE" -Path "/pets/$petId/tags" -Headers $workerHeaders -Body @{ ids = @($tagIds[0]) } | Out-Null
}

Invoke-Api -Id "PET-11" -Method "PUT" -Path "/pets/$petId/status" -Headers $workerHeaders -Body @{
    petId = $petId
    status = "SHELTERED"
    reason = "Sheltered by worker"
} | Out-Null

Invoke-Api -Id "PET-12" -Method "GET" -Path "/pets/$petId/status?page=1&size=10" -Headers $workerHeaders | Out-Null

Invoke-Api -Id "PET-08" -Method "DELETE" -Path "/pets/$petId/media/$petMediaId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "PET-13-unauthorized" -Method "DELETE" -Path "/pets/$petId" -Headers $ownerHeaders -Expected "HTTP 403 / code 403" | Out-Null
Invoke-Api -Id "PET-13" -Method "DELETE" -Path "/pets/$petId" -Headers $workerHeaders | Out-Null

# RescueTaskController
$rescueBegin = Invoke-Api -Id "RESCUE-01" -Method "PUT" -Path "/tasks" -Headers $ownerHeaders
$rescueUuid = $rescueBegin.data

$rescueUpload = Invoke-Api -Id "RESCUE-02" -Method "POST" -Path "/tasks/$rescueUuid/uploads" -Headers $ownerHeaders -Multipart @{
    file = Get-Item $ImgRescue
}
$rescueTempFile = $rescueUpload.data
if ($rescueTempFile) {
    Invoke-Api -Id "RESCUE-03" -Method "DELETE" -Path "/tasks/$rescueUuid/uploads/$rescueTempFile" -Headers $ownerHeaders | Out-Null
}
Start-Sleep -Milliseconds 1200
$rescueUpload2 = Invoke-Api -Id "RESCUE-03-reupload" -Method "POST" -Path "/tasks/$rescueUuid/uploads" -Headers $ownerHeaders -Multipart @{
    file = Get-Item $ImgRescue
}

$rescueAdd = Invoke-Api -Id "RESCUE-04" -Method "POST" -Path "/tasks" -Headers $ownerHeaders -Body @{
    id = $rescueUuid
    previousId = $null
    summary = "Rescue $suffix"
    description = "Need help for injured stray cat"
    type = "MEDICAL"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Rescue road 9"
}
$taskId = $rescueAdd.data.id

Invoke-Api -Id "RESCUE-05" -Method "GET" -Path "/tasks?page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "RESCUE-06-owner" -Method "GET" -Path "/tasks/$taskId" -Headers $ownerHeaders | Out-Null
Invoke-Api -Id "RESCUE-06-other" -Method "GET" -Path "/tasks/$taskId" -Headers $otherHeaders -Expected "HTTP 403 / code 403" | Out-Null

Invoke-Api -Id "RESCUE-07" -Method "PUT" -Path "/tasks/$taskId" -Headers $ownerHeaders -Body @{
    summary = "Rescue upd $suffix"
    description = "Updated details for injured stray cat"
    type = "OTHER"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Rescue road 10"
} | Out-Null

Invoke-Api -Id "RESCUE-08" -Method "GET" -Path "/tasks/$taskId/status" -Headers $ownerHeaders | Out-Null
Invoke-Api -Id "RESCUE-09-approved" -Method "PATCH" -Path "/tasks/$taskId/status" -Headers $workerHeaders -Body @{
    status = "APPROVED"
    reason = "Approved by worker"
} | Out-Null
Invoke-Api -Id "RESCUE-09-invalid" -Method "PATCH" -Path "/tasks/$taskId/status" -Headers $workerHeaders -Body @{
    status = "CREATED"
    reason = "Invalid rollback"
} -Expected "HTTP 422 / code 422" | Out-Null
Invoke-Api -Id "RESCUE-10" -Method "POST" -Path "/tasks/$taskId/assign" -Headers $workerHeaders -Body @{
    ids = @($otherId)
} | Out-Null
Invoke-Api -Id "RESCUE-09-processing" -Method "PATCH" -Path "/tasks/$taskId/status" -Headers $workerHeaders -Body @{
    status = "PROCESSING"
    reason = "Processing started"
} | Out-Null
Invoke-Api -Id "RESCUE-09-completed" -Method "PATCH" -Path "/tasks/$taskId/status" -Headers $workerHeaders -Body @{
    status = "COMPLETED"
    reason = "Rescue completed"
} | Out-Null
Invoke-Api -Id "RESCUE-12" -Method "DELETE" -Path "/tasks/$taskId" -Headers $workerHeaders -Expected "HTTP 422 / code 422" | Out-Null

# LostPetController
$lostBegin = Invoke-Api -Id "LOST-01" -Method "PUT" -Path "/lost/pets" -Headers $ownerHeaders
$lostUuid = $lostBegin.data
$lostUpload = Invoke-Api -Id "LOST-02" -Method "PUT" -Path "/lost/pets/$lostUuid/media" -Headers $ownerHeaders -Multipart @{
    file = Get-Item $ImgLostPet
    name = "lost-cat"
}
$lostTempFile = $lostUpload.data
if ($lostTempFile) {
    Invoke-Api -Id "LOST-03" -Method "DELETE" -Path "/lost/pets/$lostUuid/media/$lostTempFile" -Headers $ownerHeaders | Out-Null
}
Start-Sleep -Milliseconds 1200
Invoke-Api -Id "LOST-03-reupload" -Method "PUT" -Path "/lost/pets/$lostUuid/media" -Headers $ownerHeaders -Multipart @{
    file = Get-Item $ImgLostPet
    name = "lost-cat-final"
} | Out-Null

$lostAdd = Invoke-Api -Id "LOST-04" -Method "POST" -Path "/lost/pets" -Headers $ownerHeaders -Body @{
    uuid = $lostUuid
    name = "Lost Cat $suffix"
    age = 10
    sex = "FEMALE"
    type = "CAT"
    breed = "British Shorthair"
    features = "White paws"
    lostTime = (Get-Date).AddHours(-2).ToString("o")
    phone = "13810000002"
    description = "Lost near shelter"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Pet Street 1"
}
$lostPetId = $lostAdd.data.id

Invoke-Api -Id "LOST-05" -Method "PUT" -Path "/lost/pets/$lostPetId" -Headers $ownerHeaders -Body @{
    name = "Lost Cat Updated $suffix"
    age = 11
    sex = "FEMALE"
    type = "CAT"
    breed = "British Shorthair"
    features = "White paws and bell"
    lostTime = (Get-Date).AddHours(-1).ToString("o")
    phone = "13810000099"
    description = "Updated lost description"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Pet Street 1"
} | Out-Null

Invoke-Api -Id "LOST-06" -Method "GET" -Path "/lost/pets/$lostPetId/similar" -Headers $ownerHeaders | Out-Null
Invoke-Api -Id "LOST-07" -Method "POST" -Path "/lost/pets/$lostPetId/mismatch/$petId" -Headers $ownerHeaders | Out-Null
Invoke-Api -Id "LOST-08" -Method "GET" -Path "/lost/pets/$lostPetId" -Headers $ownerHeaders | Out-Null
Invoke-Api -Id "LOST-09" -Method "GET" -Path "/lost/pets?page=1&size=10" -Headers $workerHeaders | Out-Null

$claimAdd = Invoke-Api -Id "LOST-10" -Method "POST" -Path "/lost/claim" -Headers $otherHeaders -Body @{
    lostPetId = $lostPetId
    petId = $petId
    applicantPhone = "13810000003"
    reason = "This matches my observation"
}
$claimId = $claimAdd.data.id

Invoke-Api -Id "LOST-11" -Method "GET" -Path "/lost/claim/$claimId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "LOST-12" -Method "GET" -Path "/lost/claim?page=1&size=10" -Headers $workerHeaders | Out-Null
$claim2 = Invoke-Api -Id "LOST-10b" -Method "POST" -Path "/lost/claim" -Headers $otherHeaders -Body @{
    lostPetId = $lostPetId
    petId = $petId
    applicantPhone = "13810000003"
    reason = "Temporary claim to cancel"
}
$claim2Id = $claim2.data.id
Invoke-Api -Id "LOST-13" -Method "DELETE" -Path "/lost/claim/$claim2Id" -Headers $otherHeaders | Out-Null
Invoke-Api -Id "LOST-14" -Method "PATCH" -Path "/lost/claim/$claimId/approve" -Headers $workerHeaders -Body @{
    status = "PASS"
    petId = $petId
    reason = "Verified by worker"
} | Out-Null

# PublicityController
$articleAdd = Invoke-Api -Id "PUB-01" -Method "POST" -Path "/publicity/articles" -Headers $workerHeaders -Body @{
    type = "ACTIVITY"
    title = "API Activity $suffix"
    content = "This is a publicity article for API group 02."
    cover = $null
    publish = $false
}
$articleId = $articleAdd.data.id

Invoke-Api -Id "PUB-02" -Method "GET" -Path "/publicity/articles?page=1&size=10" -Headers $otherHeaders | Out-Null
Invoke-Api -Id "PUB-04" -Method "PUT" -Path "/publicity/articles/$articleId" -Headers $workerHeaders -Body @{
    title = "API Activity Updated $suffix"
    summary = "ignored by entity"
    content = "Updated activity article body."
    cover = $null
    tags = "api,test"
} | Out-Null
Invoke-Api -Id "PUB-05-published" -Method "PATCH" -Path "/publicity/articles/$articleId/PUBLISHED" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "PUB-03" -Method "GET" -Path "/publicity/articles/$articleId" -Headers $otherHeaders | Out-Null
Invoke-Api -Id "PUB-06" -Method "POST" -Path "/publicity/articles/$articleId/like" -Headers $otherHeaders | Out-Null
Invoke-Api -Id "PUB-07" -Method "DELETE" -Path "/publicity/articles/$articleId/like" -Headers $otherHeaders | Out-Null
Invoke-Api -Id "PUB-08" -Method "POST" -Path "/publicity/articles/$articleId/favorite" -Headers $otherHeaders | Out-Null
Invoke-Api -Id "PUB-10" -Method "GET" -Path "/publicity/favorites?page=1&size=10" -Headers $otherHeaders | Out-Null
Invoke-Api -Id "PUB-09" -Method "DELETE" -Path "/publicity/articles/$articleId/favorite" -Headers $otherHeaders | Out-Null
Invoke-Api -Id "PUB-11" -Method "POST" -Path "/publicity/articles/$articleId/share" -Headers $otherHeaders | Out-Null
Invoke-Api -Id "PUB-05-draft" -Method "PATCH" -Path "/publicity/articles/$articleId/DRAFT" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "PUB-12" -Method "DELETE" -Path "/publicity/articles/$articleId" -Headers $workerHeaders | Out-Null

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
$lines += "# API Test Results 02: Pet, Rescue, Lost Pet, Publicity"
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
