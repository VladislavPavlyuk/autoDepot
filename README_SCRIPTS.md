# Auto Depot - Startup Scripts

This directory contains scripts to easily start and stop the Auto Depot application.

## Available Scripts

### Windows

#### `start.bat` - Start the application
- Double-click to run, or execute from command prompt
- Automatically checks for Java installation
- Stops any existing process on port 8080
- Builds and starts the Spring Boot application

#### `start.ps1` - Start the application (PowerShell)
- Right-click and select "Run with PowerShell"
- Or execute: `powershell -ExecutionPolicy Bypass -File start.ps1`
- Same functionality as `start.bat` but with colored output

#### `stop.bat` - Stop the application
- Stops any process running on port 8080
- Useful if the application needs to be stopped manually

#### `stop.ps1` - Stop the application (PowerShell)
- PowerShell version of stop script

### Linux/Mac

#### `start.sh` - Start the application
```bash
chmod +x start.sh
./start.sh
```
- Checks for Java installation
- Stops any existing process on port 8080
- Builds and starts the application

#### `stop.sh` - Stop the application
```bash
chmod +x stop.sh
./stop.sh
```
- Stops any process running on port 8080

## Usage

### Quick Start (Windows)
1. Double-click `start.bat`
2. Wait for the application to start
3. Open browser and navigate to `http://localhost:8080`
4. Login with: `admin` / `admin`

### Quick Start (Linux/Mac)
1. Open terminal in project directory
2. Run: `chmod +x start.sh && ./start.sh`
3. Wait for the application to start
4. Open browser and navigate to `http://localhost:8080`
5. Login with: `admin` / `admin`

## Requirements

- Java 25 or later
- Maven (included via Maven Wrapper)
- Port 8080 available (or modify `application.properties`)

## Troubleshooting

### Port 8080 already in use
- Run `stop.bat` (Windows) or `stop.sh` (Linux/Mac) to stop existing instance
- Or manually kill the process using the port

### Java not found
- Install Java 25 or later
- Ensure Java is in your system PATH
- Verify with: `java -version`

### Maven wrapper not found
- Ensure you are in the project root directory
- The `mvnw.cmd` (Windows) or `mvnw` (Linux/Mac) file should be present
