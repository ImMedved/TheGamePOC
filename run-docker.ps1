param(
    [ValidateSet("DEBUG", "INFO", "WARN", "ERROR")]
    [string]$LogLevel = "INFO"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command vcxsrv.exe -ErrorAction SilentlyContinue)) {
    Write-Host "Installing VcXsrv..."
    choco install vcxsrv -y
}

if (-not (Get-Process vcxsrv -ErrorAction SilentlyContinue)) {
    Start-Process "C:\Program Files\VcXsrv\vcxsrv.exe" `
        -ArgumentList ":0 -multiwindow -clipboard -ac"
}

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

if (-not (Test-XServer)) {
    if (-not $vcxsrv) {
        Write-Host "VcXsrv is required to show JSFML windows from Docker on Windows."
        Write-Host "Install VcXsrv, then run this script again."
        exit 1
    }

    Start-Process -FilePath $vcxsrv -ArgumentList ":0 -multiwindow -ac -nowgl -silent-dup-error" -WindowStyle Hidden
    Start-Sleep -Seconds 2
}

if (-not (Test-XServer)) {
    Write-Host "X server did not open on localhost:6000. Check Windows firewall or VcXsrv settings."
    exit 1
}

$env:DISPLAY = "host.docker.internal:0.0"
$env:LOG_LEVEL = $LogLevel
docker compose up --build
