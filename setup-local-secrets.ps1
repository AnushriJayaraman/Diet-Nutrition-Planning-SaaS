param(
    [switch]$UpdateSenderEmail,
    [switch]$ConfigureDatabase
)

$ErrorActionPreference = "Stop"

$secretsDirectory = Join-Path $HOME ".nutricare"
$secretsFile = Join-Path $secretsDirectory "application-secrets.properties"

function Ensure-LocalAuthTokenSecret {
    param([string]$Path)

    $contents = if (Test-Path $Path) { Get-Content -Path $Path -Raw } else { "" }
    if ($contents -match '(?m)^app\.auth\.token-secret=.{32,}$') {
        return
    }

    $randomBytes = New-Object byte[] 48
    $randomGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $randomGenerator.GetBytes($randomBytes)
    } finally {
        $randomGenerator.Dispose()
    }
    $tokenSecret = [Convert]::ToBase64String($randomBytes)
    Add-Content -Path $Path -Value "app.auth.token-secret=$tokenSecret" -Encoding ascii
}

function Set-LocalProperty {
    param(
        [string]$Path,
        [string]$Name,
        [string]$Value
    )

    $escapedValue = $Value.Replace("\", "\\").Replace(":", "\:").Replace("=", "\=").Replace("#", "\#").Replace("!", "\!")
    if ($escapedValue.StartsWith(" ")) {
        $escapedValue = "\$escapedValue"
    }
    $lines = if (Test-Path $Path) { @(Get-Content -Path $Path) } else { @() }
    $pattern = '^\s*' + [regex]::Escape($Name) + '\s*=.*$'
    $updated = $false
    for ($index = 0; $index -lt $lines.Count; $index++) {
        if ($lines[$index] -match $pattern) {
            $lines[$index] = "$Name=$escapedValue"
            $updated = $true
        }
    }
    if (-not $updated) {
        $lines += "$Name=$escapedValue"
    }
    Set-Content -Path $Path -Value $lines -Encoding ascii
}

if ($ConfigureDatabase) {
    if (-not (Test-Path $secretsFile)) {
        throw "No local secrets file exists. Run .\setup-local-secrets.ps1 first."
    }

    $databaseUsername = Read-Host "Local MySQL username"
    if ([string]::IsNullOrWhiteSpace($databaseUsername)) {
        throw "A MySQL username is required."
    }
    $secureDatabasePassword = Read-Host "Local MySQL password (input is hidden)" -AsSecureString
    $databasePassword = [System.Net.NetworkCredential]::new("", $secureDatabasePassword).Password
    if ([string]::IsNullOrWhiteSpace($databasePassword) -or $databasePassword.Contains("`r") -or $databasePassword.Contains("`n")) {
        $databasePassword = $null
        $secureDatabasePassword.Dispose()
        throw "A MySQL password without line breaks is required."
    }

    Set-LocalProperty -Path $secretsFile -Name "spring.datasource.username" -Value $databaseUsername.Trim()
    Set-LocalProperty -Path $secretsFile -Name "spring.datasource.password" -Value $databasePassword
    $databasePassword = $null
    $secureDatabasePassword.Dispose()
    Write-Output "Local MySQL username and password saved in your Windows-restricted secrets file. Restart the backend to use them."
    return
}

if ($UpdateSenderEmail) {
    if (-not (Test-Path $secretsFile)) {
        throw "No saved Brevo settings were found. Run .\setup-local-secrets.ps1 first."
    }

    $savedSettings = Get-Content -Path $secretsFile -Raw
    if ($savedSettings -notmatch '(?m)^brevo\.api-key=') {
        throw "The saved Brevo settings file does not contain an API key. Run .\setup-local-secrets.ps1 to configure it."
    }

    $senderEmail = Read-Host "Correct verified Brevo sender email address"
    if ([string]::IsNullOrWhiteSpace($senderEmail)) {
        throw "A verified Brevo sender email address is required."
    }

    $escapedSender = $senderEmail.Trim().Replace("\", "\\")
    $updatedSettings = [System.Text.RegularExpressions.Regex]::Replace(
        $savedSettings,
        '(?m)^brevo\.sender-email=.*$',
        [System.Text.RegularExpressions.MatchEvaluator]{
            param($match)
            "brevo.sender-email=$escapedSender"
        },
        [System.Text.RegularExpressions.RegexOptions]::None
    )
    if ($updatedSettings -eq $savedSettings) {
        $updatedSettings = $savedSettings.TrimEnd("`r", "`n") + "`nbrevo.sender-email=$escapedSender`n"
    }
    Set-Content -Path $secretsFile -Value $updatedSettings -Encoding ascii
    Ensure-LocalAuthTokenSecret -Path $secretsFile
    Write-Output "Sender email updated. Your saved API key was kept unchanged."
    return
}

if (Test-Path $secretsFile) {
    Ensure-LocalAuthTokenSecret -Path $secretsFile
    Write-Output "Local Brevo settings are already saved for this Windows user."
    return
}

$senderEmail = Read-Host "Verified Brevo sender email address"
if ([string]::IsNullOrWhiteSpace($senderEmail)) {
    throw "A verified Brevo sender email address is required."
}

$secureApiKey = Read-Host "Brevo API key (input is hidden)" -AsSecureString
$apiKey = [System.Net.NetworkCredential]::new("", $secureApiKey).Password
if ([string]::IsNullOrWhiteSpace($apiKey)) {
    throw "A Brevo API key is required."
}

New-Item -ItemType Directory -Path $secretsDirectory -Force | Out-Null
$identity = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
New-Item -ItemType File -Path $secretsFile -Force | Out-Null
& icacls $secretsFile /inheritance:r /grant:r "${identity}:(F)" | Out-Null
if ($LASTEXITCODE -ne 0) {
    Remove-Item $secretsFile -Force
    $apiKey = $null
    $secureApiKey.Dispose()
    throw "Could not restrict access to the local Brevo credentials file."
}

$escapedSender = $senderEmail.Trim().Replace("\", "\\")
$escapedApiKey = $apiKey.Replace("\", "\\")
@(
    "brevo.sender-email=$escapedSender"
    "brevo.api-key=$escapedApiKey"
) | Set-Content -Path $secretsFile -Encoding ascii
Ensure-LocalAuthTokenSecret -Path $secretsFile

$apiKey = $null
$secureApiKey.Dispose()
Write-Output "Brevo settings saved securely for this Windows user. You will not need to enter them again."
