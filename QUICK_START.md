# Physiotherapy Scheduler - Quick Start Guide

## 🚀 Project Overview

You now have a complete, working **Physiotherapy Appointment Scheduling System** built with:
- **Backend**: Clojure with SQLite database, Ring/Compojure web server, buddy-auth security
- **Frontend**: ClojureScript with Reagent (React), re-frame state management  
- **Development**: REPL-driven development with hot-reloading

## 📁 Project Structure

```
physiotherapy-scheduler/
├── deps.edn                    # Clojure dependencies
├── shadow-cljs.edn            # ClojureScript build config  
├── package.json               # Node.js dependencies
├── start-dev.sh              # Development startup script
├── README.md                 # Full documentation
├── resources/config.edn      # Configuration
├── src/
│   ├── backend/              # Clojure backend
│   │   ├── core.clj         # Main server & routes
│   │   ├── config.clj       # Config loading
│   │   ├── db/core.clj      # Database layer (SQLite)
│   │   ├── api/             # REST API endpoints
│   │   └── middleware/      # Auth & error handling
│   └── physiotherapy_scheduler/frontend/  # ClojureScript frontend
│       ├── core.cljs        # Main entry point
│       ├── events.cljs      # re-frame events
│       ├── subs.cljs        # re-frame subscriptions
│       └── views/           # React components
├── dev/user.clj             # REPL utilities
├── public/                  # Static files & JS output
└── physiotherapy-dev.db     # SQLite database (auto-created)
```

## 🎯 Quick Start (3 Options)

### Option 1: Use the Start Script (Easiest)
```bash
cd physiotherapy-scheduler
./start-dev.sh
```
This starts both frontend and backend automatically.

### Option 2: Start Servers Separately 

**Terminal 1 - Backend (Clojure REPL):**
```bash
cd physiotherapy-scheduler
clj -A:dev
```
In the REPL:
```clojure
(start)  ; Starts server on http://localhost:3000
```

**Terminal 2 - Frontend (ClojureScript):**
```bash
cd physiotherapy-scheduler
npm run dev  ; Starts on http://localhost:8080
```

### Option 3: Production Build
```bash
npm run build  # Build frontend
clj -M -m backend.core  # Start backend server
```

## 🌐 Access Your Application

- **Frontend UI**: http://localhost:8080
- **Backend API**: http://localhost:3000
- **Health Check**: http://localhost:3000/health

## 🔐 Demo Login Credentials

- **Username**: `admin` **Password**: `admin123`
- **Username**: `therapist` **Password**: `therapist123`

## 📋 Features Available

✅ **Patient Management** - Add, edit, view patients  
✅ **Time Slot Management** - Create available appointment slots  
✅ **Appointment Scheduling** - Book patients into slots  
✅ **Authentication** - Secure login system  
✅ **Dashboard** - Overview with statistics  
✅ **REST API** - Full CRUD operations  
✅ **Error Handling** - Comprehensive error management  
✅ **Validation** - Data validation with Clojure Spec  

## 🛠️ Development Commands

**REPL Commands (in Clojure REPL):**
```clojure
(start)     ; Start the server
(stop)      ; Stop the server  
(restart)   ; Restart with code reload
(reset-db)  ; Reset the database
```

**Build Commands:**
```bash
npm run dev    # Start ClojureScript in watch mode
npm run build  # Build optimized ClojureScript
```

## 🔗 API Endpoints

**Authentication:**
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout

**Patients:**
- `GET /api/v1/patients` - List patients
- `POST /api/v1/patients` - Create patient  
- `GET /api/v1/patients/:id` - Get patient
- `PUT /api/v1/patients/:id` - Update patient
- `DELETE /api/v1/patients/:id` - Delete patient

**Appointments & Slots:** Similar CRUD patterns for appointments and time slots.

## 🎨 Architecture Highlights

- **Immutable Data**: All state is immutable using Clojure data structures
- **Functional Programming**: Pure functions throughout the application
- **Component-Based UI**: Reagent components with re-frame state management
- **Database**: SQLite for development (easily upgradeable to PostgreSQL/Datomic)
- **Security**: Session-based authentication with buddy-auth
- **Error Handling**: Comprehensive error boundaries and logging
- **Hot Reloading**: Both frontend and backend support live code reloading

## 🔄 Development Workflow

1. **Start development environment** (Option 1, 2, or 3 above)
2. **Edit code** - changes hot-reload automatically
3. **Use REPL** for interactive development:
   - Test functions in isolation
   - Query database directly
   - Experiment with new features
4. **View in browser** - see changes instantly

## 📖 Next Steps

1. **Explore the code** - Start with `src/backend/core.clj` and `src/physiotherapy_scheduler/frontend/core.cljs`
2. **Add features** - The architecture supports easy extension
3. **Customize styling** - Edit `public/index.html` for UI changes
4. **Database evolution** - Easy to migrate to PostgreSQL or Datomic
5. **Deploy** - Ready for deployment to any server

## 🆘 Troubleshooting

**Port conflicts?** Change ports in `resources/config.edn` and `shadow-cljs.edn`
**Database issues?** Delete `physiotherapy-dev.db` to reset  
**Dependencies?** Run `clj -A:dev -M -e "(println \"deps ok\")"` to check Clojure deps
**REPL not connecting?** Ensure you're in the project directory

## 🎉 You're Ready!

Your physiotherapy scheduler is fully functional with a modern, scalable architecture. The combination of Clojure's simplicity and power with React's user interface capabilities gives you a robust foundation for any healthcare scheduling needs.

**Happy Coding! 🚀**

