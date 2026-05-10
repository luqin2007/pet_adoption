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
$ResultJson = Join-Path $Root "test/api-test-results-01-auth-user-asset-notice-$RunId.json"
$ResultMd = Join-Path $Root "test/api-test-results-01-auth-user-asset-notice-$RunId.md"
$Avatar = Join-Path $Root "testImages/005d70677bbf3c352f7070c730f8686ad6d9314bb167a16b2f2b2588ad90fb13.jpg"
$Results = [System.Collections.Generic.List[object]]::new()

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

function Get-RedisArrayValues([string]$Text) {
    $values = [System.Collections.Generic.List[string]]::new()
    $lines = $Text -split "`r?`n"
    for ($i = 0; $i -lt $lines.Length; $i++) {
        if ($lines[$i] -match '^\$\d+' -and ($i + 1) -lt $lines.Length) {
            $values.Add($lines[$i + 1])
            $i++
        }
    }
    return $values
}

function Get-PasswordResetId([string]$Email) {
    $keysText = Invoke-RedisRaw @("KEYS", "pet_adoption:user.forgetpwd.*")
    $keys = Get-RedisArrayValues $keysText
    foreach ($key in $keys) {
        if ($key -like "pet_adoption:user.forgetpwd.code.*") {
            continue
        }
        $value = Invoke-Redis @("GET", $key)
        if ($value -eq $Email) {
            return $key.Substring("pet_adoption:user.forgetpwd.".Length)
        }
    }
    throw "Cannot find password reset id for $Email"
}

function ConvertTo-SafeData($Value) {
    if ($null -eq $Value) {
        return $null
    }
    if ($Value -is [string] -or $Value.GetType().IsPrimitive) {
        return $Value
    }
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
            ($Body | ConvertTo-Json -Depth 20 -Compress) | Set-Content -Path $jsonFile -Encoding UTF8
            $curlArgs += @("-H", "Content-Type: application/json", "--data-binary", "@$jsonFile")
        }
        $curlArgs += $uri
        $raw = & curl.exe @curlArgs
        if ($jsonFile -and (Test-Path $jsonFile)) {
            Remove-Item -LiteralPath $jsonFile -Force
        }
        $statusLine = $raw | Where-Object { $_ -match '^HTTP/' } | Select-Object -Last 1
        $statusCode = if ($statusLine -match 'HTTP/\S+\s+(\d+)') { [int]$matches[1] } else { $null }
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

        if ($null -ne $Multipart) {
            $curlArgs = @("-s", "-i", "-X", $Method)
            foreach ($header in $Headers.GetEnumerator()) {
                $curlArgs += @("-H", "$($header.Key): $($header.Value)")
            }
            foreach ($entry in $Multipart.GetEnumerator()) {
                if ($entry.Value -is [IO.FileInfo]) {
                    $curlArgs += @("-F", "$($entry.Key)=@$($entry.Value.FullName)")
                } else {
                    $curlArgs += @("-F", "$($entry.Key)=$($entry.Value)")
                }
            }
            $curlArgs += $uri
            $raw = & curl.exe @curlArgs
            $statusLine = $raw | Select-Object -First 1
            $statusCode = if ($statusLine -match 'HTTP/\S+\s+(\d+)') { [int]$matches[1] } else { $null }
            $splitIndex = [Array]::IndexOf($raw, "")
            $bodyLines = if ($splitIndex -ge 0 -and $splitIndex -lt ($raw.Length - 1)) { $raw[($splitIndex + 1)..($raw.Length - 1)] } else { @() }
            $content = ($bodyLines -join "`n")
            $parsed = $null
            if ($content.TrimStart().StartsWith("{")) {
                $parsed = $content | ConvertFrom-Json
            }
            $expectedStatus = if ($Expected -match 'HTTP\s+(\d+)') { [int]$matches[1] } else { 200 }
            $expectedCode = if ($Expected -match 'code\s+(\d+)') { [int]$matches[1] } else { 200 }
            $ok = if ($parsed) {
                $statusCode -eq $expectedStatus -and $parsed.code -eq $expectedCode
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
                data = if ($parsed) { $parsed.data } else { "multipart/content length=$($content.Length)" }
                message = if ($parsed) { $parsed.message } else { "" }
            }
            $Results.Add([pscustomobject]$result)
            return [pscustomobject]$result
        }

        $params = @{
            Method = $Method
            Uri = $uri
            Headers = $Headers
        }
        if ($null -ne $Form) {
            $params.ContentType = "application/x-www-form-urlencoded"
            $params.Body = ($Form.GetEnumerator() | ForEach-Object {
                "$([uri]::EscapeDataString($_.Key))=$([uri]::EscapeDataString([string]$_.Value))"
            }) -join "&"
        } elseif ($null -ne $Body) {
            $params.ContentType = "application/json"
            $params.Body = ($Body | ConvertTo-Json -Depth 20)
        }
        $response = Invoke-WebRequest @params
        $contentType = ""
        if ($response.Headers.ContainsKey("Content-Type")) {
            $contentType = $response.Headers["Content-Type"] -join ";"
        }
        $parsed = $null
        if ($contentType -like "application/json*") {
            $parsed = $response.Content | ConvertFrom-Json
        }
        $expectedStatus = if ($Expected -match 'HTTP\s+(\d+)') { [int]$matches[1] } else { 200 }
        $expectedCode = if ($Expected -match 'code\s+(\d+)') { [int]$matches[1] } else { 200 }
        $ok = if ($parsed) {
            $response.StatusCode -eq $expectedStatus -and $parsed.code -eq $expectedCode
        } else {
            $response.StatusCode -eq $expectedStatus
        }
        $result = [ordered]@{
            id = $Id
            method = $Method
            path = $Path
            status = [int]$response.StatusCode
            code = if ($parsed) { [int]$parsed.code } else { $null }
            expected = $Expected
            passed = $ok
            contentType = $contentType
            elapsedMs = [int]((Get-Date) - $started).TotalMilliseconds
            data = if ($parsed) { $parsed.data } else { "binary/content length=$($response.RawContentLength)" }
            message = if ($parsed) { $parsed.message } else { "" }
        }
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
    }
    $Results.Add([pscustomobject]$result)
    return [pscustomobject]$result
}

function Send-CodeAndRead([string]$Email) {
    Invoke-Api -Id "AUTH-04-send-$Email" -Method "POST" -Path "/auth/check/code?email=$([uri]::EscapeDataString($Email))" | Out-Null
    $key = "pet_adoption:user.forgetpwd.code.$Email"
    return Invoke-Redis @("GET", $key)
}

function Register-TestUser([string]$Username, [string]$Email, [string]$Password, [string]$Phone) {
    Invoke-Api -Id "AUTH-02-$Username-before" -Method "GET" -Path "/auth/check/username/$Username" | Out-Null
    Invoke-Api -Id "AUTH-03-$Username-before" -Method "GET" -Path "/auth/check/email/$([uri]::EscapeDataString($Email))" | Out-Null
    $code = Send-CodeAndRead $Email
    $form = @{
        username = $Username
        password = $Password
        email = $Email
        phone = $Phone
        code = $code
    }
    return Invoke-Api -Id "AUTH-01-$Username" -Method "POST" -Path "/auth/register" -Form $form
}

function Login-TestUser([string]$Username, [string]$Password) {
    return Invoke-Api -Id "AUTH-05-$Username" -Method "POST" -Path "/auth/login" -Body @{
        username = $Username
        password = $Password
    }
}

function Mysql-Exec([string]$Sql) {
    if (-not $MysqlUri) {
        $script:MysqlUri = Get-MysqlUriFromProperties
    }
    mysqlsh --sql --uri $MysqlUri -e $Sql | Out-Null
}

$suffix = Get-Date -Format "HHmmss"
$password = "Api@123456"
$normal = "api_normal_$suffix"
$worker = "api_worker_$suffix"
$doctor = "api_doctor_$suffix"
$volunteer = "api_volunteer_$suffix"
$admin = "api_admin_$suffix"

$normalReg = Register-TestUser $normal "$normal@example.com" $password "13800000001"
$workerReg = Register-TestUser $worker "$worker@example.com" $password "13800000002"
$doctorReg = Register-TestUser $doctor "$doctor@example.com" $password "13800000003"
$volunteerReg = Register-TestUser $volunteer "$volunteer@example.com" $password "13800000004"
$adminReg = Register-TestUser $admin "$admin@example.com" $password "13800000005"

Mysql-Exec "UPDATE user SET role = CASE username WHEN '$worker' THEN 3 WHEN '$doctor' THEN 8 WHEN '$volunteer' THEN 1 WHEN '$admin' THEN 31 ELSE role END WHERE username IN ('$worker','$doctor','$volunteer','$admin');"

$normalLogin = Login-TestUser $normal $password
$workerLogin = Login-TestUser $worker $password
$adminLogin = Login-TestUser $admin $password

$normalToken = $normalLogin.data.accessToken
$workerToken = $workerLogin.data.accessToken
$normalHeaders = @{ Authorization = "Bearer $normalToken" }
$workerHeaders = @{ Authorization = "Bearer $workerToken" }

$normalId = $normalLogin.data.id
$workerId = $workerLogin.data.id

Invoke-Api -Id "AUTH-02-existing" -Method "GET" -Path "/auth/check/username/$normal" -Expected "HTTP 200 / code 422 for existing username" | Out-Null
Invoke-Api -Id "AUTH-03-existing" -Method "GET" -Path "/auth/check/email/$([uri]::EscapeDataString("$normal@example.com"))" -Expected "HTTP 200 / code 422 for existing email" | Out-Null
Invoke-Api -Id "AUTH-08-refresh" -Method "POST" -Path "/auth/refresh" -Body @{ refreshToken = $normalLogin.data.refreshToken } | Out-Null

$resetEmail = "$normal@example.com"
Invoke-Api -Id "AUTH-06-forget" -Method "POST" -Path "/auth/forget?email=$([uri]::EscapeDataString($resetEmail))" | Out-Null
$resetId = Get-PasswordResetId $resetEmail
Invoke-Api -Id "AUTH-07-reset" -Method "POST" -Path "/auth/reset" -Body @{ id = $resetId; password = "Api@654321" } | Out-Null
Invoke-Api -Id "AUTH-05-login-new-password" -Method "POST" -Path "/auth/login" -Body @{ username = $normal; password = "Api@654321" } | Out-Null
Invoke-Api -Id "AUTH-09-logout" -Method "POST" -Path "/auth/logout" -Headers $normalHeaders -Body @{ refreshToken = $normalLogin.data.refreshToken } | Out-Null

$normalLogin2 = Login-TestUser $normal "Api@654321"
$normalToken2 = $normalLogin2.data.accessToken
$normalHeaders2 = @{ Authorization = "Bearer $normalToken2" }

Invoke-Api -Id "USER-01" -Method "GET" -Path "/users/$normalId" -Headers $normalHeaders2 | Out-Null
Invoke-Api -Id "USER-02" -Method "PUT" -Path "/users/$normalId" -Headers $normalHeaders2 -Body @{
    username = "$normal`_updated"
    password = "Api@654321"
    email = "$normal.updated@example.com"
    role = 0
} | Out-Null
Invoke-Api -Id "USER-02-stale-token" -Method "GET" -Path "/users/$normalId" -Headers $normalHeaders2 -Expected "HTTP 401 / code 401 for renamed user stale token" | Out-Null
$normalLogin3 = Login-TestUser "$normal`_updated" "Api@654321"
$normalToken3 = $normalLogin3.data.accessToken
$normalHeaders3 = @{ Authorization = "Bearer $normalToken3" }

Invoke-Api -Id "USER-03" -Method "PUT" -Path "/users/$workerId" -Headers $normalHeaders3 -Body @{
    username = "$worker`_bad"
    password = $password
    email = "$worker.bad@example.com"
    role = 3
} -Expected "HTTP 403 for unauthorized update" | Out-Null

$avatarResponse = Invoke-Api -Id "USER-04" -Method "PATCH" -Path "/users/$normalId/avatar" -Headers $normalHeaders3 -Multipart @{ avatar = Get-Item $Avatar }
$assetPath = $avatarResponse.data
if ($assetPath) {
    Invoke-Api -Id "USER-05" -Method "GET" -Path $assetPath -Expected "HTTP 200 binary asset" | Out-Null
}
Invoke-Api -Id "ASSET-02" -Method "GET" -Path "/assets/unknown/1/a.jpg" -Expected "HTTP 404" | Out-Null
Invoke-Api -Id "ASSET-03" -Method "GET" -Path "/assets/pet/1/not-exists.jpg" -Expected "HTTP 404" | Out-Null
Invoke-Api -Id "ASSET-04" -Method "GET" -Path "/assets/pet/1/..%2Fsecret.jpg" -Expected "HTTP 400 path traversal blocked by container" | Out-Null

Invoke-Api -Id "USER-06" -Method "DELETE" -Path "/users/$normalId/avatar" -Headers $normalHeaders3 | Out-Null
Invoke-Api -Id "USER-07" -Method "GET" -Path "/users?page=1&size=10" -Headers $workerHeaders | Out-Null

Invoke-Api -Id "NOTICE-01" -Method "GET" -Path "/notices?page=1&size=10" -Headers $normalHeaders3 | Out-Null
Invoke-Api -Id "NOTICE-02" -Method "GET" -Path "/notices/unread" -Headers $normalHeaders3 | Out-Null
Invoke-Api -Id "NOTICE-03" -Method "PATCH" -Path "/notices/read" -Headers $normalHeaders3 -Body @{ ids = @() } -Expected "HTTP 200 or validation result for empty ids" | Out-Null
Invoke-Api -Id "NOTICE-04" -Method "PATCH" -Path "/notices/unread" -Headers $normalHeaders3 -Body @{ ids = @() } -Expected "HTTP 200 or validation result for empty ids" | Out-Null

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
$lines += "# API Test Results 01: Auth, User, Asset, Notice"
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
