#!/bin/bash

echo "Starting Auto Depot Application..."
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 25 or later"
    exit 1
fi

# Check if Maven wrapper exists
if [ ! -f "./mvnw" ]; then
    echo "ERROR: Maven wrapper (mvnw) not found"
    echo "Please ensure you are in the project root directory"
    exit 1
fi

# Make mvnw executable
chmod +x ./mvnw

# Stop any existing instance on port 8080
echo "Checking for existing process on port 8080..."
PID=$(lsof -ti:8080 2>/dev/null || fuser 8080/tcp 2>/dev/null | awk '{print $1}' || echo "")
if [ ! -z "$PID" ]; then
    echo "Stopping process $PID on port 8080..."
    kill -9 $PID 2>/dev/null || true
    sleep 2
fi

echo ""
echo "Building and starting application..."
echo ""

# Run the application
./mvnw spring-boot:run
