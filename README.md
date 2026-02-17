# Kiro SDLS Solution

A minimal Spring Boot REST API with health check endpoint.

## Prerequisites

- Java 25+
- Maven 3.6+
- Docker & Docker Compose

## Run Locally

### Build the project
```bash
mvn clean package
```

### Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Test the health endpoint
```bash
curl http://localhost:8080/health
```

Expected response:
```json
{"status":"ok"}
```

## Run Tests

Execute all unit tests:
```bash
mvn test
```

## Run with Docker

### Option 1: Using setup script (automated)
```bash
./setup.sh
```

This will:
1. Build and start the Docker container with docker-compose
2. Wait for the health endpoint to respond (up to 30 retries)
3. Print SUCCESS or FAILURE and exit with appropriate status code

### Option 2: Manual Docker Compose
```bash
docker compose up -d --build
```

### Test the running container
```bash
curl http://localhost:8080/health
```

### Stop the container
```bash
docker compose down
```

## Project Structure

```
.
├── pom.xml                          # Maven configuration
├── Dockerfile                       # Multi-stage Docker build
├── docker-compose.yml               # Docker Compose configuration with healthcheck
├── setup.sh                         # Automated setup script
├── src/
│   ├── main/java/com/kiro/sdls/
│   │   ├── Application.java         # Spring Boot application entry point
│   │   └── controller/
│   │       └── HealthController.java # Health check REST endpoint
│   └── test/java/com/kiro/sdls/
│       └── controller/
│           └── HealthControllerTest.java # Unit tests for health endpoint
└── README.md
```

## API Endpoints

### GET /health
Returns the application health status.

**Response:** 200 OK
```json
{"status":"ok"}
```
