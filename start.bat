@echo off
echo Starting Auto Depot Application...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 25 or later
    pause
    exit /b 1
)

REM Check if Maven wrapper exists
if not exist "mvnw.cmd" (
    echo ERROR: Maven wrapper (mvnw.cmd) not found
    echo Please ensure you are in the project root directory
    pause
    exit /b 1
)

REM Start Postgres via Docker Compose
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not installed or not in PATH
    echo Please install Docker Desktop
    pause
    exit /b 1
)

echo Starting Postgres (Docker Compose)...
docker compose up -d
if %errorlevel% neq 0 (
    echo ERROR: Failed to start Postgres via Docker Compose
    pause
    exit /b 1
)

REM Remove stale PID file
if exist "app.pid" del /f /q "app.pid" >nul 2>&1

echo.
echo Building and starting application...
echo.

REM Run the application
call mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--spring.pid.file=app.pid

pause
