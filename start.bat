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

REM Stop any existing instance on port 8080
echo Checking for existing process on port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo Stopping process %%a on port 8080...
    taskkill /F /PID %%a >nul 2>&1
)

REM Wait a moment for port to be released
timeout /t 2 /nobreak >nul

echo.
echo Building and starting application...
echo.

REM Run the application
call mvnw.cmd spring-boot:run

pause
