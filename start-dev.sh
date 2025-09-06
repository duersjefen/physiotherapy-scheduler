#!/bin/bash

echo "Starting Physiotherapy Scheduler Development Environment..."
echo "========================================================="

# Function to cleanup background processes
cleanup() {
    echo "Stopping development servers..."
    if [ ! -z "$SHADOW_PID" ]; then
        kill $SHADOW_PID 2>/dev/null
    fi
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null
    fi
    exit 0
}

# Trap cleanup function on script exit
trap cleanup EXIT INT TERM

# Start shadow-cljs in the background
echo "Starting ClojureScript compiler..."
npx shadow-cljs watch app &
SHADOW_PID=$!

# Wait a moment for shadow-cljs to initialize
sleep 5

echo "Frontend available at: http://localhost:8080"
echo "Starting backend server..."

# Start backend server
clj -A:dev -M -e "(do (require 'user) (user/start) (println \"Backend server started at: http://localhost:3000\") (println \"Press Ctrl+C to stop both servers...\") (while true (Thread/sleep 1000)))"

