# Development Guide

## Quick Start

1. **Start CALVA REPL**: `Ctrl+Alt+C Ctrl+Alt+J` (Jack-in)
2. **Load development environment**: `(dev-setup)` in REPL
3. **Test API**: `(require 'test-api) (test-api/run-all-tests)`

## Development Workflow

### 1. Starting Development

```clojure
;; In CALVA REPL after Jack-in:
(dev-setup)  ; Initialize everything
;; Server starts at http://localhost:3000
```

### 2. Making Changes

- Edit code files
- Use `Ctrl+Alt+C Ctrl+Alt+L` to load current file
- Or `(refresh)` to reload all namespaces
- Server auto-restarts with `(restart)`

### 3. Testing API

```clojure
(require 'test-api)

;; Test individual endpoints
(test-api/test-health)
(test-api/test-create-slot)
(test-api/test-get-slots)

;; Run all tests
(test-api/run-all-tests)
```

### 4. Database Development

```clojure
;; Reset database
(reset-db)

;; Create test data
(create-sample-slots)

;; Check database health
(db/health-check)
```

### 5. Manual API Testing

```bash
# Health check
curl http://localhost:3000/health

# Create slot
curl -X POST -H "Content-Type: application/json" \
  -d '{"start-time":"2024-12-15T10:00:00Z","duration":60}' \
  http://localhost:3000/api/slots

# Get available slots
curl "http://localhost:3000/api/slots/available?start-date=2024-12-01T00:00:00Z&end-date=2024-12-31T23:59:59Z"
```

## Available Commands

### REPL Commands
- `(start)` - Start server
- `(stop)` - Stop server  
- `(restart)` - Restart server
- `(reset-db)` - Reset database
- `(create-sample-slots)` - Create test data
- `(dev-setup)` - Full setup

### CALVA Shortcuts
- `Ctrl+Alt+C Ctrl+Alt+J` - Jack-in (start REPL)
- `Ctrl+Alt+C Ctrl+Alt+L` - Load current file
- `Ctrl+Alt+C Ctrl+Alt+E` - Evaluate current form
- `Ctrl+Alt+C Ctrl+Alt+R` - Evaluate current top-level form
- `Alt+Enter` - Evaluate current form and print result

## Testing Strategy

1. **Unit Tests**: Add to `test/` directory
2. **Integration Tests**: Use the API testing utilities
3. **Manual Testing**: Use curl or browser

## Database

- Development: `dev.db` (configured in .vscode/settings.json)
- Production: `scheduler.db`
- Reset with `(reset-db)` anytime

## Troubleshooting

### Server won't start
```clojure
;; Check if port 3000 is in use
(stop)  ; Stop current server
;; Kill any processes on port 3000
;; Then (start) again
```

### Database issues
```clojure
(reset-db)  ; Reset database
;; Check scheduler.db file exists
```

### REPL issues
```clojure
(refresh)  ; Reload all namespaces
;; Or restart CALVA REPL
```

## Accessing from Windows Browser

Since you're using WSL2, you have several options to access your server from Windows:

### Option 1: Use WSL2 IP Address (Immediate)
- Open Windows browser
- Go to: `http://172.19.172.178:3000`
- Replace `172.19.172.178` with your WSL2 IP (run `hostname -I` in WSL2)

### Option 2: Enable Localhost Forwarding (Recommended)
1. Create file `C:\Users\<YourUsername>\.wslconfig` in Windows
2. Add this content:
   ```
   [wsl2]
   localhostForwarding=true
   ```
3. Restart WSL2: `wsl --shutdown` then reopen WSL2
4. Access via: `http://localhost:3000` from Windows

### Option 3: Use the Startup Script
```bash
./start-dev-server.sh
```
This will show you all available access URLs.

## Current Status
- ✅ Server running on WSL2: http://localhost:3000
- ✅ Windows access: http://172.19.172.178:3000
- 🔄 Localhost forwarding: Enable with .wslconfig for http://localhost:3000
