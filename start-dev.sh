#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"

echo "Starting Physiotherapy Clinic Development Environment..."
echo "Logs -> $LOG_DIR"

# Cleanup background processes and print logs
cleanup() {
  echo "\nStopping development servers..."
  if [ -n "${SHADOW_PID-}" ]; then
    kill ${SHADOW_PID} 2>/dev/null || true
  fi
  if [ -n "${BACKEND_PID-}" ]; then
    kill ${BACKEND_PID} 2>/dev/null || true
  fi

  echo "\n--- Last 200 lines: frontend log ---"
  tail -n 200 "$LOG_DIR/frontend.log" 2>/dev/null || true
  echo "\n--- Last 200 lines: backend log ---"
  tail -n 200 "$LOG_DIR/backend.log" 2>/dev/null || true
  exit 0
}

trap cleanup EXIT INT TERM

# Start shadow-cljs
echo "Starting ClojureScript compiler (shadow-cljs)..."
npx shadow-cljs watch app > "$LOG_DIR/frontend.log" 2>&1 &
SHADOW_PID=$!
echo "shadow-cljs PID: $SHADOW_PID"

# Start backend server
echo "Starting backend server..."
clj -A:dev -M -m backend.core > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "backend PID: $BACKEND_PID"

echo "Waiting for services to become available..."

# Wait for compilation
sleep 15

echo ""
echo "🎉 Development servers are starting up!"
echo ""
echo "📱 Frontend: http://localhost:8080/index.html"
echo "🔧 Shadow-cljs Dashboard: http://localhost:9630"
echo "🚀 Backend API: http://localhost:8085"
echo ""
echo "The website is now publicly accessible - no login required!"
echo "Staff can login via the 'Staff Login' link for admin features."
echo ""
echo "Press Ctrl+C to stop both servers and print recent logs."

while true; do sleep 1; done
