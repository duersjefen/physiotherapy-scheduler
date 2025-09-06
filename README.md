# Physiotherapy Scheduler

A comprehensive physiotherapy appointment scheduling system built with Clojure/ClojureScript, Datomic Local, and re-frame.

## Features

- **Patient Management**: Add, edit, and manage patient information
- **Appointment Scheduling**: Schedule and manage appointments with time slot management
- **Authentication**: Secure login system with session management
- **Modern UI**: React-based frontend with re-frame state management
- **Database**: Datomic Local for robust data storage
- **API**: RESTful API with comprehensive error handling

## Tech Stack

### Backend
- **Clojure 1.11.1**: Main programming language
- **Datomic Local**: Database for data storage
- **Ring/Compojure**: Web server and routing
- **Buddy**: Authentication and security
- **Aero**: Configuration management
- **Timbre**: Logging
- **Cheshire**: JSON handling

### Frontend
- **ClojureScript**: Frontend programming language
- **Reagent**: React wrapper for ClojureScript
- **re-frame**: State management framework
- **Shadow-cljs**: Build tool for ClojureScript

## Development Setup

### Prerequisites
- Java 11 or higher
- Node.js 16 or higher
- Clojure CLI tools

### Installation

1. **Clone and setup the project:**
   ```bash
   cd physiotherapy-scheduler
   ```

2. **Install Node.js dependencies:**
   ```bash
   npm install
   ```

3. **Install Clojure dependencies:**
   ```bash
   clj -A:dev -M -e "(println \"Dependencies downloaded\")"
   ```

### Development Workflow

#### Backend Development

1. **Start a Clojure REPL:**
   ```bash
   clj -A:dev
   ```

2. **In the REPL, start the server:**
   ```clojure
   (start)
   ```

3. **The server will be available at http://localhost:3000**

4. **REPL Development Commands:**
   - `(start)` - Start the server
   - `(stop)` - Stop the server
   - `(restart)` - Restart the server with code reload
   - `(reset-db)` - Reset the database

#### Frontend Development

1. **Start the ClojureScript compiler in watch mode:**
   ```bash
   npm run dev
   ```

2. **Open http://localhost:8080 for the frontend**

3. **The development server will auto-reload on file changes**

### Production Build

1. **Build the frontend:**
   ```bash
   npm run build
   ```

2. **Start the backend server:**
   ```bash
   clj -M -m backend.core
   ```

## Configuration

Configuration is managed through `resources/config.edn` using environment-based profiles:

```edn
{:server {:port #profile {:dev 3000
                         :prod #env PORT}}
 :database {:uri #profile {:dev "datomic:local//physiotherapy-dev"
                          :prod #env DATABASE_URL}}
 :auth {:secret #profile {:dev "dev-secret-key"
                         :prod #env AUTH_SECRET}}}
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Patients (v1)
- `GET /api/v1/patients` - List all patients
- `POST /api/v1/patients` - Create new patient
- `GET /api/v1/patients/:id` - Get patient by ID
- `PUT /api/v1/patients/:id` - Update patient
- `DELETE /api/v1/patients/:id` - Delete patient

### Appointments (v1)
- `GET /api/v1/appointments` - List all appointments
- `POST /api/v1/appointments` - Create new appointment
- `GET /api/v1/appointments/:id` - Get appointment by ID
- `PUT /api/v1/appointments/:id` - Update appointment
- `DELETE /api/v1/appointments/:id` - Delete appointment

### Time Slots (v1)
- `GET /api/v1/slots` - List all slots
- `POST /api/v1/slots` - Create new slot
- `GET /api/v1/slots/:id` - Get slot by ID
- `PUT /api/v1/slots/:id` - Update slot
- `DELETE /api/v1/slots/:id` - Delete slot

## Demo Credentials

For development, use these demo credentials:
- **Admin**: username: `admin`, password: `admin123`
- **Therapist**: username: `therapist`, password: `therapist123`

## Project Structure

```
├── deps.edn                 # Clojure dependencies
├── shadow-cljs.edn          # ClojureScript build config
├── package.json             # Node.js dependencies
├── resources/
│   └── config.edn          # Configuration
├── src/
│   ├── backend/            # Clojure backend code
│   │   ├── core.clj        # Main server
│   │   ├── config.clj      # Configuration
│   │   ├── db/
│   │   │   └── core.clj    # Database layer
│   │   ├── api/            # API endpoints
│   │   └── middleware/     # Middleware
│   └── frontend/           # ClojureScript frontend code
│       ├── core.cljs       # Main entry point
│       ├── db.cljs         # Frontend state schema
│       ├── events.cljs     # re-frame events
│       ├── subs.cljs       # re-frame subscriptions
│       └── views/          # UI components
├── dev/
│   └── user.clj            # Development utilities
└── public/                 # Static assets
    └── index.html          # HTML template
```

## Contributing

1. Follow the existing code style and conventions
2. Write tests for new features
3. Use the REPL for interactive development
4. Ensure proper error handling and validation

## License

This project is licensed under the MIT License.
