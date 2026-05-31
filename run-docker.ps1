param(
    [ValidateSet("DEBUG", "INFO", "WARN", "ERROR")]
    [string]$LogLevel = "INFO",
    [switch]$SoftwareRendering
)

$ErrorActionPreference = "Stop"

function Test-XServer {
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $connect = $client.BeginConnect("127.0.0.1", 6000, $null, $null)
        if (-not $connect.AsyncWaitHandle.WaitOne(1000, $false)) {
            $client.Close()
            return $false
        }
        $client.EndConnect($connect)
        $client.Close()
        return $true
    } catch {
        return $false
    }
}

function Find-VcXsrv {
    $candidates = @(
        "$env:ProgramFiles\VcXsrv\vcxsrv.exe",
        "${env:ProgramFiles(x86)}\VcXsrv\vcxsrv.exe"
    )

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path $candidate)) {
            return $candidate
        }
    }

    return $null
}

$vcxsrv = Find-VcXsrv

if (-not $vcxsrv) {
    if (-not (Get-Command choco -ErrorAction SilentlyContinue)) {
        Write-Host "VcXsrv is required. Install Chocolatey or VcXsrv manually, then run this script again."
        exit 1
    }

    Write-Host "Installing VcXsrv..."
    choco install vcxsrv -y
    $vcxsrv = Find-VcXsrv
}

if (-not $vcxsrv) {
    Write-Host "VcXsrv installation did not complete. Run PowerShell as Administrator and try again."
    exit 1
}

if (-not (Test-XServer)) {
    $xServerArguments = ":0 -multiwindow -clipboard -ac -silent-dup-error"
    if ($SoftwareRendering) {
        $xServerArguments += " -nowgl"
    }

    Start-Process -FilePath $vcxsrv `
        -ArgumentList $xServerArguments `
        -WindowStyle Hidden
    Start-Sleep -Seconds 2
}

if (-not (Test-XServer)) {
    Write-Host "X server did not open on localhost:6000. Check Windows firewall or VcXsrv settings."
    exit 1
}

New-Item -ItemType Directory -Path ".\logs" -Force | Out-Null

$env:DISPLAY = "host.docker.internal:0.0"
$env:LOG_LEVEL = $LogLevel
$env:LIBGL_ALWAYS_SOFTWARE = if ($SoftwareRendering) { "1" } else { "0" }

docker compose up --build
