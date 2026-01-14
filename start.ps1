# PowerShell script to start Auto Depot Application

Write-Host "Starting Auto Depot Application..." -ForegroundColor Green
Write-Host ""

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java not found"
    }
} catch {
    Write-Host "ERROR: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 25 or later" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if Maven wrapper exists
if (-not (Test-Path "mvnw.cmd")) {
    Write-Host "ERROR: Maven wrapper (mvnw.cmd) not found" -ForegroundColor Red
    Write-Host "Please ensure you are in the project root directory" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Stop any existing instance on port 8080
Write-Host "Checking for existing process on port 8080..." -ForegroundColor Yellow
$existingProcess = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | 
    Select-Object -ExpandProperty OwningProcess -Unique

if ($existingProcess) {
    Write-Host "Stopping process $existingProcess on port 8080..." -ForegroundColor Yellow
    Stop-Process -Id $existingProcess -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "Building and starting application..." -ForegroundColor Green
Write-Host ""

# Run the application
& .\mvnw.cmd spring-boot:run

Read-Host "`nPress Enter to exit"
