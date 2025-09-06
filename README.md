# Physiotherapy Scheduler

A self-hosted appointment booking system built with Clojure/ClojureScript, designed specifically for physiotherapy practices and similar healthcare services.

## Features

### For Clients
- **Easy Appointment Booking**: Browse available time slots and book appointments online
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices
- **Date Range Filtering**: Search for appointments within specific date ranges
- **Real-time Availability**: See up-to-date slot availability
- **Email Confirmation**: Receive booking confirmations via email

### For Practitioners (Admin)
- **Slot Management**: Create, view, and manage appointment slots
- **Client Overview**: View all booked appointments with client details
- **Flexible Scheduling**: Set custom appointment durations (15-180 minutes)
- **Dashboard**: Admin panel for comprehensive appointment management
- **Status Tracking**: Monitor appointment statuses (available, booked, cancelled)

### Technical Features
- **Self-hosted**: Full control over your data and infrastructure
- **SQLite Database**: Lightweight, reliable data storage
- **Docker Support**: Easy deployment with containerization
- **REST API**: Clean API architecture for potential integrations
- **Responsive UI**: Built with Tailwind CSS for modern, accessible design

## Technology Stack

### Backend
- **Clojure**: Robust, functional programming language
- **Ring/Jetty**: HTTP server and middleware
- **Compojure**: Elegant routing library
- **next.jdbc**: Modern database access
- **SQLite**: Embedded database (easily replaceable)
- **Cheshire**: JSON handling

### Frontend
- **ClojureScript**: Frontend development in Clojure
- **Reagent**: React wrapper for ClojureScript
- **re-frame**: State management framework
- **shadow-cljs**: Build tool and development environment
- **Tailwind CSS**: Utility-first CSS framework

### Infrastructure
- **Docker**: Containerization for easy deployment
- **Docker Compose**: Multi-service orchestration
- **Traefik**: Reverse proxy and load balancer (optional)

## Quick Start

### Prerequisites
- **Docker & Docker Compose**: For containerized deployment
- **OR** Java 11+ and Clojure CLI tools for local development
- **Node.js 16+**: For frontend build tools

### Option 1: Docker Deployment (Recommended)

1. **Clone and configure**:
   ```bash
   git clone <your-repo>
   cd physiotherapy-scheduler
   ```

2. **Start with Docker Compose**:
   ```bash
   docker-compose up -d
   ```

3. **Access the application**:
   - Main app: http://localhost:3000
   - Traefik dashboard: http://localhost:8080 (if enabled)

### Option 2: Local Development

1. **Install dependencies**:
   ```bash
   # Backend dependencies
   clojure -P

   # Frontend dependencies  
   npm install
   ```

2. **Start the database**:
   ```bash
   # Database will be created automatically on first run
   ```

3. **Start frontend development**:
   ```bash
   npm run dev
   # Or: npx shadow-cljs watch app
   ```

4. **Start backend server**:
   ```bash
   clojure -M:dev
   # Then in REPL: (user/start)
   ```

5. **Access the application**:
   - Frontend dev server: http://localhost:8080
   - Backend API: http://localhost:3000

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | `jdbc:sqlite:data/scheduler.db` | Database connection string |
| `HTTP_PORT` | `3000` | HTTP server port |
| `LOG_LEVEL` | `info` | Logging level (trace, debug, info, warn, error) |

### Database Configuration

The application uses SQLite by default, but can be configured for other databases:

```bash
# PostgreSQL example
DATABASE_URL="jdbc:postgresql://localhost:5432/scheduler?user=admin&password=secret"

# MySQL example  
DATABASE_URL="jdbc:mysql://localhost:3306/scheduler?user=admin&password=secret"
```

## API Documentation

### Public Endpoints

#### Get Available Slots
```http
GET /api/slots/available?start-date=2024-12-01T00:00:00Z&end-date=2024-12-31T23:59:59Z
```

Response:
```json
{
  "slots": [
    {
      "id": 1,
      "start_time": "2024-12-15T10:00:00Z",
      "duration_minutes": 60,
      "status": "available"
    }
  ]
}
```

#### Book Appointment
```http
POST /api/slots/:id/book
Content-Type: application/json

{
  "client_name": "John Doe", 
  "client_email": "john@example.com"
}
```

### Admin Endpoints

#### Create Slot
```http
POST /api/slots
Content-Type: application/json

{
  "start_time": "2024-12-15T10:00:00Z",
  "duration": 60
}
```

#### Get All Slots
```http
GET /api/admin/slots
```

## Deployment

### Docker Production Deployment

1. **Build production image**:
   ```bash
   docker build -t physiotherapy-scheduler:latest .
   ```

2. **Run with production settings**:
   ```bash
   docker run -d \
     --name scheduler \
     -p 3000:3000 \
     -v scheduler_data:/app/data \
     -v scheduler_logs:/app/logs \
     -e LOG_LEVEL=warn \
     physiotherapy-scheduler:latest
   ```

### AWS ECS Deployment

1. **Push image to ECR**:
   ```bash
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com
   docker tag physiotherapy-scheduler:latest <account>.dkr.ecr.us-east-1.amazonaws.com/physiotherapy-scheduler:latest
   docker push <account>.dkr.ecr.us-east-1.amazonaws.com/physiotherapy-scheduler:latest
   ```

2. **Create ECS task definition** (see `aws/` directory for examples)

3. **Deploy to ECS cluster**

### Traditional Server Deployment

1. **Build uberjar**:
   ```bash
   clojure -T:build uber
   ```

2. **Deploy JAR**:
   ```bash
   java -jar target/physiotherapy-scheduler.jar
   ```

## Development

### Project Structure
```
physiotherapy-scheduler/
├── src/
│   ├── app/
│   │   ├── core.clj              # Main application entry
│   │   ├── db/
│   │   │   └── core.clj          # Database layer
│   │   ├── api/
│   │   │   └── slots.clj         # API endpoints
│   │   └── frontend/
│   │       └── core.cljs         # ClojureScript frontend
├── resources/
│   ├── migrations/
│   │   └── 001-initial-schema.sql # Database migrations
│   └── public/
│       └── index.html            # HTML template
├── dev/
│   └── user.clj                  # Development utilities
├── deps.edn                      # Clojure dependencies
├── shadow-cljs.edn              # Frontend build config
├── Dockerfile                   # Container definition
└── docker-compose.yml          # Multi-service setup
```

### Development Workflow

1. **Start REPL**: `clojure -M:dev`
2. **Start frontend**: `npm run dev`
3. **Make changes**: Edit `.clj` or `.cljs` files
4. **Test changes**: Refresh browser or evaluate in REPL
5. **Run tests**: `clojure -M:test`

### Adding Features

#### New API Endpoint
1. Add route to `app.api.slots`
2. Implement handler function
3. Add corresponding frontend HTTP call
4. Update re-frame events and subscriptions

#### Database Changes
1. Create new migration file in `resources/migrations/`
2. Update database functions in `app.db.core`
3. Test migration with `(user/migrate!)`

## Troubleshooting

### Common Issues

**Database connection errors**:
- Check `DATABASE_URL` environment variable
- Ensure SQLite file permissions are correct
- Verify database directory exists

**Frontend not loading**:
- Check browser console for JavaScript errors
- Verify ClojureScript compilation: `npm run build`
- Ensure backend is running on correct port

**Docker build failures**:
- Check Docker daemon is running
- Verify internet connection for dependency downloads
- Clean Docker cache: `docker system prune`

### Logs

- **Application logs**: Check `/app/logs/` in container
- **Docker logs**: `docker logs physiotherapy-scheduler`
- **Development logs**: Check REPL output

## Security Considerations

- **Admin Access**: Currently no authentication - implement auth before production
- **HTTPS**: Use reverse proxy (Traefik/nginx) with SSL certificates
- **Database**: Use strong passwords for production databases
- **Backups**: Regular database backups are included in Docker Compose

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Support

For issues and questions:
- Create GitHub issue for bugs/feature requests
- Check existing documentation and troubleshooting section
- Review Docker and Clojure community resources

---

**Note**: This application is designed for small practices. For large-scale deployments, consider additional security measures, authentication systems, and database optimizations.
