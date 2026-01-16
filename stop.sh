#!/bin/bash

echo "Stopping Auto Depot Application..."
echo ""

# Stop Spring Boot by PID file
if [ -f "./app.pid" ]; then
    APP_PID=$(cat ./app.pid)
    if [ ! -z "$APP_PID" ]; then
        echo "Stopping process $APP_PID..."
        kill -15 "$APP_PID" 2>/dev/null || true
        sleep 2
        if kill -0 "$APP_PID" 2>/dev/null; then
            kill -9 "$APP_PID" 2>/dev/null || true
        fi
        echo "Process stopped."
    fi
    rm -f ./app.pid
else
    echo "No PID file found (app.pid). Skipping app shutdown."
fi

echo "Stopping Postgres (Docker Compose)..."
docker compose stop >/dev/null 2>&1
