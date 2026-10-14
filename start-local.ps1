$ErrorActionPreference = "Stop"

& (Join-Path $PSScriptRoot "setup-local-secrets.ps1")

$occupiedPorts = Get-NetTCPConnection -State Listen -LocalPort 8080, 5173 -ErrorAction SilentlyContinue
if ($occupiedPorts) {
    $portDetails = $occupiedPorts |
        Group-Object LocalPort |
        ForEach-Object {
            $processIds = ($_.Group.OwningProcess | Sort-Object -Unique) -join ", "
            "port $($_.Name) (PID $processIds)"
        }
    throw "Local app ports are already in use: $($portDetails -join '; '). Stop the existing backend/frontend (including VS Code Run sessions), then run this script again."
}

Push-Location $PSScriptRoot
try {
    npm run dev
} finally {
    Pop-Location
}
