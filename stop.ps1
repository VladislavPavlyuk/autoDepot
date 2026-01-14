# PowerShell script to stop Auto Depot Application

Write-Host "Stopping Auto Depot Application..." -ForegroundColor Yellow
Write-Host ""

# Find and stop process on port 8080
$existingProcess = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | 
    Select-Object -ExpandProperty OwningProcess -Unique

if ($existingProcess) {
    Write-Host "Stopping process $existingProcess on port 8080..." -ForegroundColor Yellow
    Stop-Process -Id $existingProcess -Force
    if ($?) {
        Write-Host "Process stopped successfully." -ForegroundColor Green
    } else {
        Write-Host "Failed to stop process." -ForegroundColor Red
    }
} else {
    Write-Host "No process found on port 8080." -ForegroundColor Yellow
}

Read-Host "`nPress Enter to exit"
