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

# Start Postgres via Docker Compose
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: Docker is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Docker Desktop" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Starting Postgres (Docker Compose)..." -ForegroundColor Green
docker compose up -d | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to start Postgres via Docker Compose" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Remove stale PID file
if (Test-Path "app.pid") {
    Remove-Item "app.pid" -Force -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "Building and starting application..." -ForegroundColor Green
Write-Host ""

# Run the application
& .\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--spring.pid.file=app.pid

Read-Host "`nPress Enter to exit"
