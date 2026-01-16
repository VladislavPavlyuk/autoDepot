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

# Start Postgres via Docker Compose
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed or not in PATH"
    echo "Please install Docker Desktop"
    exit 1
fi

echo "Starting Postgres (Docker Compose)..."
docker compose up -d
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start Postgres via Docker Compose"
    exit 1
fi

# Remove stale PID file
rm -f ./app.pid

echo ""
echo "Building and starting application..."
echo ""

# Run the application
./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.pid.file=app.pid
