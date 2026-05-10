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
$ResultJson = Join-Path $Root "test/api-test-results-04-adopt-volunteer-$RunId.json"
$ResultMd = Join-Path $Root "test/api-test-results-04-adopt-volunteer-$RunId.md"
$Results = [System.Collections.Generic.List[object]]::new()

$ImgPetCover = Join-Path $Root "testImages/005d70677bbf3c352f7070c730f8686ad6d9314bb167a16b2f2b2588ad90fb13.jpg"
$ImgAgreement = Join-Path $Root "testImages/0c526c6fef62bce303082cf2a0ffeaefe596e415cf460674841e24cf8d84f563.jpg"
$ImgSign = Join-Path $Root "testImages/0ddd22359d33d90a778e0019a99d352e01b20852c4d2a7c8b4c8c6fa94334d9e.jpg"

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
$worker = "api4_worker_$suffix"
$adopter = "api4_adopter_$suffix"
$foster = "api4_foster_$suffix"
$volunteer = "api4_volunteer_$suffix"

Assert-ApiPreflight

Register-TestUser $worker "$worker@example.com" $password "13812000001" | Out-Null
Register-TestUser $adopter "$adopter@example.com" $password "13812000002" | Out-Null
Register-TestUser $foster "$foster@example.com" $password "13812000003" | Out-Null
Register-TestUser $volunteer "$volunteer@example.com" $password "13812000004" | Out-Null

Mysql-Exec "UPDATE user SET role = CASE username WHEN '$worker' THEN 3 ELSE role END WHERE username IN ('$worker');"

$workerLogin = Login-TestUser $worker $password
$adopterLogin = Login-TestUser $adopter $password
$fosterLogin = Login-TestUser $foster $password
$volunteerLogin = Login-TestUser $volunteer $password

$workerHeaders = @{ Authorization = "Bearer $($workerLogin.data.accessToken)" }
$adopterHeaders = @{ Authorization = "Bearer $($adopterLogin.data.accessToken)" }
$fosterHeaders = @{ Authorization = "Bearer $($fosterLogin.data.accessToken)" }
$volunteerHeaders = @{ Authorization = "Bearer $($volunteerLogin.data.accessToken)" }

$workerId = $workerLogin.data.id
$adopterId = $adopterLogin.data.id
$volunteerId = $volunteerLogin.data.id

$now = Get-Date
$recruitStart = $now.AddMinutes(-10)
$recruitEnd = $now.AddDays(10)
$shiftStart = $now.AddDays(2)
$shiftEnd = $shiftStart.AddHours(2)
$shiftStart2 = $shiftStart.AddHours(1)
$shiftEnd2 = $shiftEnd.AddHours(1)
$followPlan = $now.AddDays(5)
$periodEnd = $now
$periodStart = $now.AddDays(-30)

$petAdd = Invoke-Api -Id "PRE-PET-01" -Method "POST" -Path "/pets" -Headers $workerHeaders -Body @{
    name = "Adopt Pet $suffix"
    age = 10
    sex = "FEMALE"
    type = "CAT"
    breed = "Domestic Short Hair"
    description = "API group 04 adopt pet"
    health = "GOOD"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Shelter adoption area"
}
$petId = $petAdd.data.id
Invoke-Api -Id "PRE-PET-02" -Method "POST" -Path "/pets/$petId/media" -Headers $workerHeaders -Multipart @{ file = Get-Item $ImgPetCover; name = "adopt-cover"; description = "adopt cover"; isCover = "true" } | Out-Null
Invoke-Api -Id "PRE-PET-03" -Method "PUT" -Path "/pets/$petId/status" -Headers $workerHeaders -Body @{ petId = $petId; status = "SHELTERED"; reason = "Ready for adopt api tests" } | Out-Null

$adoptAdd = Invoke-Api -Id "ADOPT-01" -Method "POST" -Path "/adopt/adopt" -Headers $adopterHeaders -Body @{
    petId = $petId
    applicantPhone = "13812000002"
    requirement = "Indoor home with regular check-in"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Futian"
    detailAddress = "Adopter address $suffix"
}
$adoptId = $adoptAdd.data.id
Invoke-Api -Id "ADOPT-02" -Method "GET" -Path "/adopt/adopt/$adoptId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ADOPT-03" -Method "GET" -Path "/adopt/adopt?pet=$petId&user=$adopterId&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ADOPT-04-invalid" -Method "PATCH" -Path "/adopt/adopt/$adoptId/AGREEMENT_SIGNED" -Headers $workerHeaders -Expected "HTTP 422 / code 422" | Out-Null
Invoke-Api -Id "ADOPT-04" -Method "PATCH" -Path "/adopt/adopt/$adoptId/PASS" -Headers $workerHeaders | Out-Null

$breadingAdd = Invoke-Api -Id "ADOPT-05" -Method "POST" -Path "/adopt/breading" -Headers $fosterHeaders -Body @{
    petName = "Breading Pet $suffix"
    petAge = 6
    petType = "DOG"
    petBreed = "Corgi"
    petDescription = "Friendly foster pet"
    applicantPhone = "13812000003"
    time0 = $now.AddDays(3).ToString("o")
    time1 = $now.AddDays(20).ToString("o")
}
$breadingId = $breadingAdd.data.id
Invoke-Api -Id "ADOPT-06" -Method "GET" -Path "/adopt/breading/$breadingId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ADOPT-07" -Method "GET" -Path "/adopt/breading?user=$($fosterLogin.data.id)&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "ADOPT-08" -Method "PATCH" -Path "/adopt/breading/$breadingId/PASS" -Headers $workerHeaders | Out-Null

$recruitmentAdd = Invoke-Api -Id "VOL-01" -Method "POST" -Path "/volunteers/recruitments" -Headers $workerHeaders -Body @{
    title = "Volunteer Recruitment $suffix"
    description = "Need help with follow visits and shelter support"
    requirement = "Reliable communicator"
    headcount = 3
    startTime = $recruitStart.ToString("o")
    endTime = $recruitEnd.ToString("o")
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Shelter volunteer center"
}
$recruitmentId = $recruitmentAdd.data.id
Invoke-Api -Id "VOL-02" -Method "GET" -Path "/volunteers/recruitments/$recruitmentId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "VOL-03" -Method "GET" -Path "/volunteers/recruitments?status=DRAFT&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "VOL-04" -Method "PUT" -Path "/volunteers/recruitments/$recruitmentId" -Headers $workerHeaders -Body @{
    title = "Volunteer Recruitment Updated $suffix"
    description = "Updated recruitment description"
    requirement = "Reliable communicator and patient follow-up"
    headcount = 4
    startTime = $recruitStart.ToString("o")
    endTime = $recruitEnd.AddDays(2).ToString("o")
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Shelter volunteer center updated"
} | Out-Null
Invoke-Api -Id "VOL-05" -Method "PATCH" -Path "/volunteers/recruitments/$recruitmentId/PUBLISHED" -Headers $workerHeaders | Out-Null

$applicationAdd = Invoke-Api -Id "VOL-06" -Method "POST" -Path "/volunteers/applications" -Headers $volunteerHeaders -Body @{
    recruitmentId = $recruitmentId
    realName = "Volunteer $suffix"
    phone = "13812000004"
    sex = "FEMALE"
    age = 24
    profession = "Designer"
    experience = "Pet care and event support"
    skills = "communication, pet-care"
    availableTimeDesc = "Weekends and evenings"
    motivation = "Support follow-up visits"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Volunteer address $suffix"
}
$applicationId = $applicationAdd.data.id
Invoke-Api -Id "VOL-07" -Method "GET" -Path "/volunteers/applications?recruitment=$recruitmentId&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "VOL-08" -Method "GET" -Path "/volunteers/applications/$applicationId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "VOL-09-1" -Method "PATCH" -Path "/volunteers/applications/$applicationId" -Headers $workerHeaders -Body @{ status = "UNDER_REVIEW"; reason = "Initial screening" } | Out-Null
Invoke-Api -Id "VOL-09-2" -Method "PATCH" -Path "/volunteers/applications/$applicationId" -Headers $workerHeaders -Body @{ status = "APPROVED"; reason = "Qualified and available" } | Out-Null

$profileId = [long](Require-MysqlScalar "select cast(id as char) as value from volunteer_profile where user_id = $volunteerId order by id desc limit 1;" "volunteerProfileId")
Invoke-Api -Id "VOL-10" -Method "GET" -Path "/volunteers/profiles?status=ACTIVE&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "VOL-11" -Method "GET" -Path "/volunteers/profiles/$profileId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "VOL-12" -Method "PUT" -Path "/volunteers/profiles/$profileId" -Headers $volunteerHeaders -Body @{
    realName = "Volunteer Updated $suffix"
    sex = "FEMALE"
    phone = "13812000999"
    skills = "communication, driving"
    serviceIntention = "Follow visit and adoption support"
    availableTimeDesc = "Weekends"
    remark = "Updated by self"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Baoan"
    detailAddress = "Volunteer updated address"
} | Out-Null
Invoke-Api -Id "VOL-13-1" -Method "POST" -Path "/volunteers/profiles/$profileId/DISABLED" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "VOL-13-2" -Method "POST" -Path "/volunteers/profiles/$profileId/ACTIVE" -Headers $workerHeaders | Out-Null

$shiftAdd = Invoke-Api -Id "SHIFT-01" -Method "POST" -Path "/volunteers/shifts" -Headers $workerHeaders -Body @{
    volunteerId = $volunteerId
    taskType = "OTHER"
    title = "Adoption follow prep $suffix"
    content = "Prepare visit checklist"
    startTime = $shiftStart.ToString("o")
    endTime = $shiftEnd.ToString("o")
    taskStartTime = $shiftStart.ToString("o")
    taskEndTime = $shiftEnd.ToString("o")
    remark = "Initial assignment"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detailAddress = "Volunteer task address"
}
$shiftId = $shiftAdd.data.id
Invoke-Api -Id "SHIFT-02" -Method "GET" -Path "/volunteers/shifts?volunteer=$volunteerId&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "SHIFT-03" -Method "GET" -Path "/volunteers/shifts/$shiftId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "SHIFT-04" -Method "PUT" -Path "/volunteers/shifts/$shiftId" -Headers $workerHeaders -Body @{
    volunteerId = $volunteerId
    startTime = $shiftStart2.ToString("o")
    endTime = $shiftEnd2.ToString("o")
    province = "Guangdong"
    city = "Shenzhen"
    district = "Futian"
    detailAddress = "Volunteer task address updated"
} | Out-Null
Invoke-Api -Id "SHIFT-05-1" -Method "PATCH" -Path "/volunteers/shifts/$shiftId" -Headers $volunteerHeaders -Body @{ status = "CONFIRMED"; reason = "I can attend" } | Out-Null
Invoke-Api -Id "SHIFT-05-2" -Method "PATCH" -Path "/volunteers/shifts/$shiftId" -Headers $workerHeaders -Body @{ status = "IN_PROGRESS"; reason = "Task started" } | Out-Null
Invoke-Api -Id "SHIFT-05-3" -Method "PATCH" -Path "/volunteers/shifts/$shiftId" -Headers $workerHeaders -Body @{ status = "COMPLETED"; reason = "Task completed" } | Out-Null

$recordAdd = Invoke-Api -Id "SHIFT-06" -Method "POST" -Path "/volunteers/shifts/$shiftId/records" -Headers $volunteerHeaders -Body @{
    startTime = $shiftStart2.ToString("o")
    endTime = $shiftEnd2.ToString("o")
    actualHours = 2.0
    summary = "Completed support work"
    content = "Prepared checklists and reviewed forms"
    problem = "No major issues"
    suggestion = "Keep checklist template"
}
$recordId = $recordAdd.data.id
Invoke-Api -Id "SHIFT-07" -Method "GET" -Path "/volunteers/records?volunteer=$volunteerId&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "SHIFT-08" -Method "PATCH" -Path "/volunteers/records/$recordId" -Headers $workerHeaders -Body @{ status = "APPROVED"; reason = "Verified" } | Out-Null
Invoke-Api -Id "SHIFT-09" -Method "GET" -Path "/volunteers/records/$recordId" -Headers $workerHeaders | Out-Null

$rewardAdd = Invoke-Api -Id "REWARD-01" -Method "POST" -Path "/volunteers/rewards" -Headers $workerHeaders -Body @{
    volunteerId = $volunteerId
    periodStart = $periodStart.ToString("o")
    periodEnd = $periodEnd.ToString("o")
    serviceCount = 1
    totalHours = 2.0
    rewardType = "CERTIFICATE"
    rewardValue = "Volunteer Star"
    rewardReason = "Excellent first service"
    remark = "API test reward"
}
$rewardId = $rewardAdd.data.id
Invoke-Api -Id "REWARD-02" -Method "GET" -Path "/volunteers/rewards?volunteer=$volunteerId&page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "REWARD-03" -Method "GET" -Path "/volunteers/rewards/$rewardId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "REWARD-04" -Method "POST" -Path "/volunteers/rewards/$rewardId/issue" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "REWARD-04-repeat" -Method "POST" -Path "/volunteers/rewards/$rewardId/issue" -Headers $workerHeaders -Expected "HTTP 422 / code 422" | Out-Null

$agreementUuid = (Invoke-Api -Id "ADOPT-09" -Method "PUT" -Path "/adopt/agreement" -Headers $workerHeaders).data
$tempUpload = Invoke-Api -Id "ADOPT-10" -Method "POST" -Path "/adopt/agreement/upload/$agreementUuid" -Headers $workerHeaders -Multipart @{ file = Get-Item $ImgAgreement }
$tempName = $tempUpload.data
if ($tempName) {
    Invoke-Api -Id "ADOPT-11" -Method "DELETE" -Path "/adopt/agreement/upload/$agreementUuid/$tempName" -Headers $workerHeaders | Out-Null
}
Start-Sleep -Milliseconds 1000
$tempUpload2 = Invoke-Api -Id "ADOPT-11-reupload" -Method "POST" -Path "/adopt/agreement/upload/$agreementUuid" -Headers $workerHeaders -Multipart @{ file = Get-Item $ImgAgreement }
$paperAgreement = Invoke-Api -Id "ADOPT-12-paper" -Method "POST" -Path "/adopt/agreement" -Headers $workerHeaders -Body @{
    uuid = $agreementUuid
    parentId = $adoptId
    parentType = "ADOPT"
    type = "PAPER"
    fileOrder = @($tempUpload2.data)
}
$paperAgreementId = $paperAgreement.data.id
$electronicAgreement = Invoke-Api -Id "ADOPT-12-electronic" -Method "POST" -Path "/adopt/agreement" -Headers $workerHeaders -Body @{
    parentId = $breadingId
    parentType = "BREADING"
    type = "ELECTRONIC"
    content = "Electronic breading agreement $suffix"
}
$electronicAgreementId = $electronicAgreement.data.id
Invoke-Api -Id "ADOPT-13" -Method "PUT" -Path "/adopt/agreement/$electronicAgreementId" -Headers $workerHeaders -Body @{
    content = "Updated electronic breading agreement $suffix"
} | Out-Null
Start-Sleep -Milliseconds 1100
$paperFiles = Invoke-Api -Id "ADOPT-14" -Method "POST" -Path "/adopt/agreement/$paperAgreementId/files" -Headers $workerHeaders -Multipart @{ page = "1"; file = Get-Item $ImgSign }
$paperFileIds = @($paperFiles.data | ForEach-Object { $_.id })
if ($paperFileIds.Count -ge 2) {
    $reversed = @($paperFileIds[1], $paperFileIds[0])
    Invoke-Api -Id "ADOPT-16" -Method "PUT" -Path "/adopt/agreement/$paperAgreementId/files/order" -Headers $workerHeaders -Body @{ fileOrder = $reversed } | Out-Null
    Invoke-Api -Id "ADOPT-15" -Method "DELETE" -Path "/adopt/agreement/$paperAgreementId/files/$($reversed[0])" -Headers $workerHeaders | Out-Null
}
Start-Sleep -Milliseconds 1100
Invoke-Api -Id "ADOPT-17" -Method "PATCH" -Path "/adopt/agreement/$paperAgreementId/sign" -Headers $workerHeaders -Multipart @{ sign = Get-Item $ImgPetCover } | Out-Null
$agreementGet = Invoke-Api -Id "ADOPT-18" -Method "GET" -Path "/adopt/agreement/$paperAgreementId" -Headers $workerHeaders
$agreementAsset = @($agreementGet.data.files)[0].assetUrl
if ($agreementAsset) {
    Invoke-Api -Id "ADOPT-18-asset" -Method "GET" -Path $agreementAsset -Expected "HTTP 200 paper asset" | Out-Null
}
Invoke-Api -Id "ADOPT-19" -Method "GET" -Path "/adopt/agreement?parent=$adoptId&page=1&size=10" -Headers $workerHeaders | Out-Null

Invoke-Api -Id "FOLLOW-00" -Method "PATCH" -Path "/adopt/adopt/$adoptId/TRACKING" -Headers $workerHeaders | Out-Null
$followAdd = Invoke-Api -Id "FOLLOW-01" -Method "POST" -Path "/adopt/follow/adopt/$adoptId" -Headers $workerHeaders -Body @{
    volunteerId = $volunteerId
    planTime = $followPlan.ToString("o")
    remark = "First follow visit"
}
$followId = $followAdd.data.id
Invoke-Api -Id "FOLLOW-02-1" -Method "PUT" -Path "/adopt/follow/$followId" -Headers $workerHeaders -Body @{
    workerId = $workerId
    volunteerId = $volunteerId
    planTime = $followPlan.AddDays(1).ToString("o")
    status = "NOTIFIED"
    remark = "Volunteer notified"
} | Out-Null
Invoke-Api -Id "FOLLOW-02-2" -Method "PUT" -Path "/adopt/follow/$followId" -Headers $volunteerHeaders -Body @{
    workerId = $workerId
    volunteerId = $volunteerId
    planTime = $followPlan.AddDays(1).ToString("o")
    status = "IN_PROGRESS"
    remark = "Visit in progress"
} | Out-Null
Invoke-Api -Id "FOLLOW-03" -Method "GET" -Path "/adopt/follow/$followId" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "FOLLOW-04" -Method "GET" -Path "/adopt/follow?adopt=$adoptId&page=1&size=10" -Headers $workerHeaders | Out-Null
$followRecord = Invoke-Api -Id "FOLLOW-05" -Method "POST" -Path "/adopt/follow/$followId/record" -Headers $volunteerHeaders -Body @{
    visitTime = $followPlan.AddDays(1).ToString("o")
    summary = "Pet adapted well"
    lifeStatus = "Stable"
    healthStatus = "Good"
    risk = "Low"
    suggestion = "Continue current routine"
}
Invoke-Api -Id "FOLLOW-05-dup" -Method "POST" -Path "/adopt/follow/$followId/record" -Headers $volunteerHeaders -Body @{
    visitTime = $followPlan.AddDays(1).ToString("o")
    summary = "Duplicate"
    lifeStatus = "Stable"
    healthStatus = "Good"
    risk = "Low"
    suggestion = "Duplicate"
} -Expected "HTTP 409 / code 409" | Out-Null
Invoke-Api -Id "FOLLOW-06" -Method "GET" -Path "/adopt/follow/$followId/record?page=1&size=10" -Headers $workerHeaders | Out-Null
Invoke-Api -Id "FOLLOW-07" -Method "GET" -Path "/adopt/follow/record?volunteer=$volunteerId&adopt=$adoptId&page=1&size=10" -Headers $workerHeaders | Out-Null

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
$lines += "# API Test Results 04: Adopt Breading and Volunteer"
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
