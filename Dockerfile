# Multi-stage Dockerfile for Physiotherapy Scheduler
# Stage 1: Build ClojureScript frontend
FROM node:18-alpine AS frontend-builder

WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci --only=production

# Copy shadow-cljs config and source
COPY shadow-cljs.edn ./
COPY src/app/frontend/ ./src/app/frontend/

# Build production ClojureScript
RUN npx shadow-cljs release app

# Stage 2: Build Clojure backend
FROM clojure:openjdk-11-tools-deps AS backend-builder

WORKDIR /app

# Copy deps file and download dependencies
COPY deps.edn ./
RUN clojure -P

# Copy source code
COPY src/ ./src/
COPY resources/ ./resources/

# Copy frontend build from previous stage
COPY --from=frontend-builder /app/resources/public/js/ ./resources/public/js/

# Create uberjar
RUN clojure -T:build uber

# Stage 3: Runtime image
FROM openjdk:11-jre-slim

# Install SQLite and other runtime dependencies
RUN apt-get update && \
    apt-get install -y sqlite3 curl && \
    rm -rf /var/lib/apt/lists/*

# Create application user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Create app directory
WORKDIR /app

# Copy application jar
COPY --from=backend-builder /app/target/physiotherapy-scheduler.jar ./app.jar

# Create directories for data and logs
RUN mkdir -p /app/data /app/logs && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:3000/health || exit 1

# Expose port
EXPOSE 3000

# Environment variables
ENV DATABASE_URL="jdbc:sqlite:/app/data/scheduler.db"
ENV HTTP_PORT=3000
ENV LOG_LEVEL=info
ENV JAVA_OPTS="-Xmx512m -XX:+UseG1GC"

# Start application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
