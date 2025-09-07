#!/usr/bin/env pwsh
$ErrorActionPreference = 'Stop'

# dev.ps - PowerShell equivalent of start-dev.sh

try {
    $RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
}
catch {
    # Fallback when run from interactive session: use current location
    $RootDir = Get-Location
}

$LogDir = Join-Path $RootDir "logs"
New-Item -ItemType Directory -Path $LogDir -Force | Out-Null

Write-Host "Starting Physiotherapy Clinic Development Environment..."
Write-Host "Logs -> $LogDir"

$frontendLog = Join-Path $LogDir "frontend.log"
$backendLog = Join-Path $LogDir "backend.log"

Write-Host "Starting ClojureScript compiler (shadow-cljs)..."
# Build a command that pipes stderr to stdout and appends to the log file via Tee-Object
$shadowCmd = "npx shadow-cljs watch app 2>&1 | Tee-Object -FilePath '$frontendLog'"
$shadow = Start-Process -FilePath "pwsh" -ArgumentList @("-NoLogo", "-NoProfile", "-Command", $shadowCmd) -PassThru
Write-Host "shadow-cljs PID: $($shadow.Id)"

Write-Host "Starting backend server..."
$backendCmd = "clj -A:dev -M -m backend.core 2>&1 | Tee-Object -FilePath '$backendLog'"
$backend = Start-Process -FilePath "pwsh" -ArgumentList @("-NoLogo", "-NoProfile", "-Command", $backendCmd) -PassThru
Write-Host "backend PID: $($backend.Id)"

try {
    Write-Host "Waiting for services to become available..."
    Start-Sleep -Seconds 15

    Write-Host ""
    Write-Host "🎉 Development servers are starting up!"
    Write-Host ""
    Write-Host "📱 Frontend: http://localhost:8080/index.html"
    Write-Host "🔧 Shadow-cljs Dashboard: http://localhost:9630"
    Write-Host "🚀 Backend API: http://localhost:8085"
    Write-Host ""
    Write-Host "Press Ctrl+C to stop both servers and print recent logs."

    while ($true) { Start-Sleep -Seconds 1 }

}
finally {
    Write-Host "`nStopping development servers..."
    if ($shadow -and -not $shadow.HasExited) {
        try { $shadow.Kill() } catch { }
    }
    if ($backend -and -not $backend.HasExited) {
        try { $backend.Kill() } catch { }
    }

    Write-Host "`n--- Last 200 lines: frontend log ---"
    if (Test-Path $frontendLog) { Get-Content -Path $frontendLog -Tail 200 -ErrorAction SilentlyContinue | ForEach-Object { Write-Host $_ } }
    else { Write-Host "(no frontend log found)" }

    Write-Host "`n--- Last 200 lines: backend log ---"
    if (Test-Path $backendLog) { Get-Content -Path $backendLog -Tail 200 -ErrorAction SilentlyContinue | ForEach-Object { Write-Host $_ } }
    else { Write-Host "(no backend log found)" }
}
