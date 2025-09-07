# Serenity Massage Therapy Booking System

A comprehensive massage therapy booking platform built with Clojure/ClojureScript, featuring an advanced booking system with multi-step UX, admin dashboard, and rigorous testing.

## Features

### 🌟 Core Features
- **Massage Services**: Swedish, Deep Tissue, Sports, Hot Stone, Prenatal, and Aromatherapy massages
- **3-Step Booking System**: Service selection → Time selection → Customer details
- **Admin Dashboard**: Comprehensive management interface for bookings and schedules
- **Responsive Design**: Mobile-friendly interface with modern CSS styling

### 🔧 Technical Features
- **ClojureScript Frontend**: Built with re-frame and reagent
- **Clojure Backend**: Ring/Jetty server with SQLite database
- **Real-time Updates**: Live booking status updates
- **Data Validation**: Comprehensive input validation and error handling

## Tech Stack

### Backend
- **Clojure**: Main programming language
- **SQLite**: Database for data storage
- **Ring/Jetty**: Web server and routing
- **Compojure**: API routing
- **Cheshire**: JSON handling

### Frontend
- **ClojureScript**: Frontend programming language
- **Reagent**: React wrapper for ClojureScript
- **re-frame**: State management framework
- **Shadow-cljs**: Build tool for ClojureScript

## Quick Start

### Prerequisites
- Java 11+ (for Clojure)
- Node.js 14+ (for Shadow-CLJS)
- Clojure CLI tools

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd physiotherapy-scheduler
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development servers**

   **Backend Server:**
   ```bash
   clojure -M -m backend.core
   ```
   Backend will be available at: http://localhost:5000

   **Frontend Development:**
   ```bash
   npx shadow-cljs watch app
   ```
   Frontend will be available at: http://localhost:8080

## VS Code Development

### Using VS Code Tasks

1. **Start Backend Server:**
   - Press `Ctrl+Shift+P` → "Tasks: Run Task" → "Start Backend Server"

2. **Compile Frontend:**
   - Press `Ctrl+Shift+P` → "Tasks: Run Task" → "Frontend Watch"

3. **Build for Production:**
   ```bash
   npx shadow-cljs release app
   ```

## Database Setup

The application uses SQLite with automatic schema initialization. The database file `physiotherapy-dev.db` will be created automatically on first run.

### Schema
- **massage_bookings**: Customer bookings with service type, date/time, customer details
- **slots**: Available time slots for booking
- **users**: User authentication (future enhancement)

## Application Structure

### Frontend (ClojureScript)
```
src/frontend/
├── core.cljs          # Main app initialization
├── db.cljs            # App state management
├── events.cljs        # Re-frame events
├── subs.cljs          # Re-frame subscriptions
├── utils/
│   └── http.cljs      # HTTP utility functions
└── views/
    ├── main.cljs      # Main layout component
    ├── home.cljs      # Public website & booking system
    ├── admin.cljs     # Admin dashboard
    ├── login.cljs     # Authentication
    └── ...
```

### Backend (Clojure)
```
src/backend/
├── core.clj           # Main server
├── config.clj         # Configuration
├── api/
│   └── v1/
│       ├── appointments.clj  # Booking API endpoints
│       ├── patients.clj      # Customer management
│       └── slots.clj         # Time slot management
├── db/
│   └── core.clj       # Database operations
└── middleware/
    ├── auth.clj       # Authentication middleware
    └── error_handling.clj  # Error handling
```

## API Endpoints

### Booking System
- `GET /api/v1/slots` - Get available time slots
- `POST /api/v1/appointments` - Create new booking
- `GET /api/v1/appointments` - List all bookings (admin)

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

## Testing

### Test Structure
The application includes comprehensive tests covering:

- **Unit Tests**: Database operations, API validation
- **Integration Tests**: Complete booking flow testing
- **Frontend Tests**: Re-frame events and subscriptions

### Running Tests

**Backend Tests:**
```bash
clojure -M -m clojure.test.runner
```

**Frontend Tests:**
```bash
npx shadow-cljs compile test
npx shadow-cljs node-repl
```

### Test Files
- `test/backend/db/core_test.clj` - Database operation tests
- `test/backend/api/v1/appointments_test.clj` - API endpoint tests
- `test/frontend/events_test.cljs` - Frontend event tests
- `test/integration/booking_flow_test.clj` - End-to-end booking tests

## Admin Dashboard

Access the admin interface at: `http://localhost:8080/admin`

### Admin Features
- **Booking Overview**: View all customer bookings
- **Schedule Management**: Manage available time slots
- **Statistics Dashboard**: Booking analytics and insights
- **Customer Management**: View and edit customer information
- **Service Management**: Configure massage services and pricing

## Configuration

### Environment Variables
- `PORT` - Server port (default: 5000)
- `DATABASE_URL` - Database connection string
- `JWT_SECRET` - JWT signing secret for authentication

### Development Configuration
Edit `src/backend/config.clj` for development settings:
```clojure
{:port 5000
 :database-url "physiotherapy-dev.db"
 :cors-origins ["http://localhost:8080"]}
```

## Deployment

### Production Build
```bash
# Build frontend
npx shadow-cljs release app

# Build backend (uberjar)
clojure -T:build uber
```

### Docker Deployment
```dockerfile
FROM openjdk:11-jre-slim
COPY target/app.jar /app.jar
EXPOSE 5000
CMD ["java", "-jar", "/app.jar"]
```

## Development Workflow

1. **Start development servers** (backend + frontend watch)
2. **Make changes** to source files
3. **Hot reload** will automatically update the frontend
4. **Test changes** using the test suite
5. **Commit changes** with descriptive messages

### Code Style
- Follow standard Clojure conventions
- Use meaningful variable and function names
- Add docstrings to public functions
- Keep functions small and focused

## Troubleshooting

### Common Issues

**Port conflicts:**
- Backend default port: 5000
- Frontend default port: 8080
- Shadow-CLJS server: 9630

**Database issues:**
- Delete `physiotherapy-dev.db` to reset schema
- Check file permissions in project directory

**Frontend compilation:**
- Clear Shadow-CLJS cache: `npx shadow-cljs stop`
- Restart watch: `npx shadow-cljs watch app`

### Debug Tools
- **Shadow-CLJS Dashboard**: http://localhost:9630
- **Browser DevTools**: F12 for frontend debugging
- **REPL**: Connect to running development environment

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make changes and add tests
4. Run the test suite: `npm test`
5. Commit changes: `git commit -m "Add feature"`
6. Push to branch: `git push origin feature-name`
7. Create a Pull Request

## Future Enhancements

- [ ] Email confirmation system for bookings
- [ ] Calendar integration (Google Calendar, Outlook)
- [ ] Payment processing integration
- [ ] SMS notifications for appointment reminders
- [ ] Multi-therapist support
- [ ] Customer loyalty program
- [ ] Online payment and invoicing
- [ ] Mobile app development

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the GitHub repository
- Contact the development team
- Check the troubleshooting section above

---

**Built with ❤️ using Clojure and ClojureScript**
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

## Environment and local development (.env)

This project supports local environment overrides via a `.env` file at the project root. `start-dev.sh` will source `.env` if present and prefer those values over `resources/config.edn` defaults.

Recommended `.env` for development:

```env
PORT=8085
FRONTEND_PORT=8080
DATABASE_FILE=physiotherapy-dev.db
AUTH_SECRET=dev-secret-key-change-in-production
```

If an environment variable is not set, `start-dev.sh` falls back to the dev values found in `resources/config.edn` and then to safe defaults (backend: 3000, frontend: 8080).


