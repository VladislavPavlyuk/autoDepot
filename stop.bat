@echo off
echo Stopping Auto Depot Application...
echo.

REM Find and stop process on port 8080
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo Stopping process %%a on port 8080...
    taskkill /F /PID %%a
    if %errorlevel% equ 0 (
        echo Process stopped successfully.
    ) else (
        echo Failed to stop process or process not found.
    )
    goto :done
)

echo No process found on port 8080.

:done
pause
