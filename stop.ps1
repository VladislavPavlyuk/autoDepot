# PowerShell script to stop Auto Depot Application

Write-Host "Stopping Auto Depot Application..." -ForegroundColor Yellow
Write-Host ""

# Stop Spring Boot by PID file
if (Test-Path "app.pid") {
    $appPid = Get-Content "app.pid" -ErrorAction SilentlyContinue
    if ($appPid) {
        Write-Host "Stopping process $appPid..." -ForegroundColor Yellow
        Stop-Process -Id $appPid -Force -ErrorAction SilentlyContinue
        if ($?) {
            Write-Host "Process stopped successfully." -ForegroundColor Green
        } else {
            Write-Host "Failed to stop process." -ForegroundColor Red
        }
    }
    Remove-Item "app.pid" -Force -ErrorAction SilentlyContinue
} else {
    Write-Host "No PID file found (app.pid). Skipping app shutdown." -ForegroundColor Yellow
}

Write-Host "Stopping Postgres (Docker Compose)..." -ForegroundColor Yellow
docker compose stop | Out-Null

Read-Host "`nPress Enter to exit"
