#!/bin/bash

echo "Stopping Auto Depot Application..."
echo ""

# Find and stop process on port 8080
PID=$(lsof -ti:8080 2>/dev/null || fuser 8080/tcp 2>/dev/null | awk '{print $1}' || echo "")

if [ ! -z "$PID" ]; then
    echo "Stopping process $PID on port 8080..."
    kill -9 $PID 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "Process stopped successfully."
    else
        echo "Failed to stop process."
    fi
else
    echo "No process found on port 8080."
fi
