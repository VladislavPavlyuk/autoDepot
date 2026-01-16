@echo off
echo Stopping Auto Depot Application...
echo.

REM Stop Spring Boot by PID file
if exist "app.pid" (
    set /p APP_PID=<app.pid
    if not "%APP_PID%"=="" (
        echo Stopping process %APP_PID%...
        taskkill /F /PID %APP_PID% >nul 2>&1
        if %errorlevel% equ 0 (
            echo Process stopped successfully.
        ) else (
            echo Failed to stop process or process not found.
        )
    )
    del /f /q "app.pid" >nul 2>&1
) else (
    echo No PID file found (app.pid). Skipping app shutdown.
)

echo Stopping Postgres (Docker Compose)...
docker compose stop >nul 2>&1

pause
