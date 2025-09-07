#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"

# Load local env overrides if present
ENV_FILE="$ROOT_DIR/.env"
if [ -f "$ENV_FILE" ]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

echo "Starting Physiotherapy Scheduler Development Environment..."
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

# Helper: check port in use
port_in_use() {
  local port=$1
  ss -ltn | awk '{print $4}' | grep -E ":${port}$|:${port}\b" >/dev/null 2>&1
}

CONFIG_FILE="$ROOT_DIR/resources/config.edn"

# Helper to extract a raw dev value from config.edn for a given key (e.g. :port or :frontend-port)
extract_raw_dev() {
  local key="$1"
  if [ -f "$CONFIG_FILE" ]; then
    # find the line with :dev within the key block and print the rest of the line after :dev
    local raw
    raw=$(grep -A3 "$key" "$CONFIG_FILE" | grep ":dev" || true)
    # raw may be like ':dev 3000' or ':dev #env PORT' or ':dev "string"'
    echo "$raw" | sed -E 's/.*:dev[[:space:]]*//'
  fi
}

# Resolve a raw token to a usable port value
resolve_port() {
  local raw="$1"
  local fallback="$2"
  raw="${raw## }" # trim leading spaces
  raw="${raw%% }" # trim trailing spaces (simple)

  if [[ -z "$raw" ]]; then
    echo "$fallback"
    return
  fi

  # If raw starts with #env, next token is env var name
  if echo "$raw" | grep -q '^#env'; then
    envname=$(echo "$raw" | awk '{print $2}')
    # expand indirect
    val="${!envname-}"
    if [[ -n "$val" ]]; then
      echo "$val"
      return
    else
      echo "$fallback"
      return
    fi
  fi

  # If raw is a quoted string, strip quotes
  if echo "$raw" | grep -q '^".*"$'; then
    raw=$(echo "$raw" | sed -E 's/^"(.*)"$/\1/')
    echo "$raw"
    return
  fi

  # Otherwise assume it's a literal (like 3000)
  echo "$raw"
}

# Determine backend port: prefer $PORT (env/.env), fallback to config.edn dev value, default 3000
if [ -n "${PORT-}" ]; then
  BACKEND_PORT=$PORT
else
  raw=$(extract_raw_dev ":port")
  BACKEND_PORT=$(resolve_port "$raw" 3000)
fi

# Determine frontend port: prefer $FRONTEND_PORT (env/.env), fallback to config.edn dev value, default 8080
if [ -n "${FRONTEND_PORT-}" ]; then
  FRONTEND_PORT=$FRONTEND_PORT
else
  rawf=$(extract_raw_dev ":frontend-port")
  FRONTEND_PORT=$(resolve_port "$rawf" 8080)
fi

# Export ports for the started processes
export PORT=$BACKEND_PORT
export SHADOW_CLJS_HTTP_PORT=$FRONTEND_PORT

if port_in_use "$BACKEND_PORT"; then
  echo "Port $BACKEND_PORT appears in use. If this is unexpected, stop the occupying process and retry." >&2
fi

echo "Starting ClojureScript compiler (shadow-cljs)..."
npx shadow-cljs watch app > "$LOG_DIR/frontend.log" 2>&1 &
SHADOW_PID=$!
echo "shadow-cljs PID: $SHADOW_PID"

echo "Starting backend server..."
clj -A:dev -M -m backend.core > "$LOG_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "backend PID: $BACKEND_PID"

echo "Waiting for services to become available..."

# Wait for backend to respond on health endpoint
MAX_TRIES=30
SLEEP=1
TRIES=0
until curl -s --fail http://localhost:$BACKEND_PORT/health >/dev/null 2>&1; do
  TRIES=$((TRIES+1))
  if [ $TRIES -ge $MAX_TRIES ]; then
    echo "Backend did not become healthy after $MAX_TRIES seconds. Printing logs..." >&2
    echo "\n--- frontend log ---"
    tail -n 200 "$LOG_DIR/frontend.log" || true
    echo "\n--- backend log ---"
    tail -n 200 "$LOG_DIR/backend.log" || true
    exit 1
  fi
  sleep $SLEEP
done

echo "Backend healthy at http://localhost:$BACKEND_PORT/health"
echo "Frontend available at: http://localhost:$FRONTEND_PORT (shadow-cljs logs: $LOG_DIR/frontend.log)"
echo "Press Ctrl+C to stop both servers and print recent logs."

# Keep the script running so trap can cleanup on Ctrl+C
while true; do sleep 1; done
