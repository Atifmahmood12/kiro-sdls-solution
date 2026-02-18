# SDLC Demonstration Project - Planning Document

## Executive Summary

This document outlines a bounded feature demonstration that showcases a complete agentic SDLC lifecycle using the existing Kiro SDLS Spring Boot service. The demonstration will add a simple **Task Management API** with two endpoints, proving the full workflow from requirements → design → implementation → testing → CI/CD → Docker deployment.

The feature is intentionally minimal to keep the demonstration focused while still being meaningful enough to show real-world patterns including data persistence, validation, error handling, and comprehensive testing.

**Demo Scope Clarification:** This plan documents the full SDLC workflow for agent execution. The 10-12 minute demo assumes pre-built implementation and focuses on:
- Showing the spec-driven workflow artifacts (requirements, design, tasks)
- Running the application locally via setup.sh
- Demonstrating the API endpoints with live curl commands
- Validating CI/CD pipeline results

---

## Current State (Baseline Solution)

### What Exists Today

The repository contains a minimal but production-ready Spring Boot REST API with the following characteristics:

**Application Stack:**
- Java 25 + Spring Boot 3.3.0
- Maven build system
- Single health check endpoint (`GET /health`)
- Returns `{"status":"ok"}` with HTTP 200

**Quality Gates:**
- Checkstyle configured with strict rules (120 char line length, 500 line file limit, naming conventions)
- Unit tests using Spring MockMvc
- Zero current violations
- CI runs `mvn verify` (tests + checkstyle)

**Infrastructure:**
- Multi-stage Dockerfile (builder + runtime)
- Docker Compose with health checks
- Automated setup script (`setup.sh`) with retry logic
- GitHub Actions CI pipeline that:
  - Runs tests and quality checks
  - Builds Docker image
  - Validates health endpoint in container

**Project Structure:**
```
src/main/java/com/kiro/sdls/
├── Application.java              # Spring Boot entry point
└── controller/
    └── HealthController.java     # Single REST endpoint

src/test/java/com/kiro/sdls/
└── controller/
    └── HealthControllerTest.java # MockMvc integration test
```

**Current Capabilities:**
- ✅ Builds and runs locally via Maven
- ✅ Runs in Docker with automated health checks
- ✅ CI/CD pipeline validates every commit
- ✅ Code quality enforcement via Checkstyle
- ✅ Automated testing framework in place

**Current Limitations:**
- No business logic beyond health check
- No data persistence layer
- No domain models or services
- No validation or error handling patterns
- No demonstration of CRUD operations

---

## Target State (What We Will Build)

### Feature: Simple Task Management API

We will add a lightweight task management system with two endpoints that demonstrate common REST API patterns without requiring external dependencies.

**New Endpoints:**

1. **POST /tasks** - Create a new task
   - Request body: `{"title": "string", "description": "string"}`
   - Response: `{"id": "uuid", "title": "string", "description": "string", "status": "PENDING", "createdAt": "timestamp"}`
   - Status: 201 Created

2. **GET /tasks/{id}** - Retrieve a task by ID
   - Response: Task object (same as POST response)
   - Status: 200 OK or 404 Not Found

**Technical Implementation:**

- **Domain Model:** `Task` entity with id, title, description, status, createdAt
- **Repository:** In-memory `TaskRepository` using ConcurrentHashMap (no database required)
- **Service Layer:** `TaskService` with business logic and validation
- **Controller:** `TaskController` with proper REST conventions
- **Validation:** Input validation using Bean Validation annotations
- **Error Handling:** Global exception handler for 404 and validation errors
- **Tests:** Unit tests for service layer + integration tests for controllers

**Files Expected to Change/Add:**

New files (~8):
- `src/main/java/com/kiro/sdls/model/Task.java`
- `src/main/java/com/kiro/sdls/model/TaskStatus.java`
- `src/main/java/com/kiro/sdls/repository/TaskRepository.java`
- `src/main/java/com/kiro/sdls/service/TaskService.java`
- `src/main/java/com/kiro/sdls/controller/TaskController.java`
- `src/main/java/com/kiro/sdls/exception/GlobalExceptionHandler.java`
- `src/main/java/com/kiro/sdls/exception/TaskNotFoundException.java`
- `src/main/java/com/kiro/sdls/dto/CreateTaskRequest.java`

Test files (~3):
- `src/test/java/com/kiro/sdls/service/TaskServiceTest.java`
- `src/test/java/com/kiro/sdls/controller/TaskControllerTest.java`
- `src/test/java/com/kiro/sdls/repository/TaskRepositoryTest.java`

Modified files (~2):
- `pom.xml` (add validation dependency)
- `README.md` (document new endpoints)

**Total: ~13 files** (well under 20 file constraint)

---

## Acceptance Criteria

### Functional Requirements

1. **Task Creation**
   - System accepts POST requests to `/tasks` with title and description
   - System generates unique UUID for each task
   - System sets status to PENDING by default
   - System records creation timestamp
   - System returns 201 Created with task object
   - System validates title is not empty and 1-200 characters (400 Bad Request if invalid)
   - System validates description is 0-1000 characters
   - System validates title is not only whitespace

2. **Task Retrieval**
   - System accepts GET requests to `/tasks/{id}`
   - System returns 200 OK with task object if found
   - System returns 404 Not Found if task doesn't exist
   - System returns proper error message in response body

3. **Data Persistence**
   - Tasks persist in memory for the application lifecycle
   - Multiple tasks can be created and retrieved independently
   - Task IDs are unique across all tasks

### Non-Functional Requirements

4. **Code Quality**
   - All code passes Checkstyle validation (zero violations)
   - Code follows existing project conventions
   - Proper separation of concerns (controller → service → repository)

5. **Testing**
   - Minimum 1 test per acceptance criterion (criteria 1-3)
   - TaskController: All endpoints tested (POST /tasks, GET /tasks/{id}, error cases)
   - TaskService: All public methods tested
   - All tests pass in CI pipeline

6. **CI/CD**
   - GitHub Actions pipeline runs successfully
   - `mvn verify` passes (tests + checkstyle)
   - Docker image builds successfully
   - Health endpoint still works in Docker

7. **Docker Deployment**
   - setup.sh completes successfully with new endpoints
   - Health check (/health) still passes after task endpoints added
   - Docker container serves both /health and /tasks endpoints
   - Container starts and responds within setup.sh timeout

8. **Documentation**
   - README updated with new endpoint documentation
   - Request/response examples provided
   - Local testing instructions included

9. **Human Review Gate**
   - All code changes reviewed before CI/merge
   - Test results validated before deployment
   - Approval checkpoint after implementation phase

---

## Demo Steps (High Level)

### Phase 1: Requirements & Design
1. Create spec file in `.kiro/specs/task-management/requirements.md`
2. Define user stories and acceptance criteria
3. Create design document with API contracts and architecture
4. Review and approve design

### Phase 2: Implementation
1. Create domain model (Task, TaskStatus)
2. Implement repository layer (in-memory storage)
3. Implement service layer (business logic)
4. Implement controller layer (REST endpoints)
5. Add validation and error handling
6. Add DTO for request/response

### Phase 3: Testing
1. Write unit tests for repository
2. Write unit tests for service layer
3. Write integration tests for controller
4. Verify all tests pass locally
5. Verify Checkstyle passes

### Phase 3.5: Human Review & Approval Gate
1. Review all test results
2. Review code changes against acceptance criteria
3. Validate implementation matches spec
4. **GATE:** Approval required before proceeding to CI/deployment

### Phase 4: CI/CD Validation
1. Commit changes to Git
2. Push to GitHub (or run CI locally)
3. Verify GitHub Actions pipeline passes
4. Verify Docker build succeeds

### Phase 5: Local Demo (10 minutes)
1. Show spec artifacts (requirements, design, tasks) - 2 min
2. Code walkthrough (implementation highlights) - 2 min
3. Show CI results (GitHub Actions) - 1 min
4. Live demo - 5 min:
   - Run `./setup.sh` to start Docker container
   - Test health endpoint: `curl http://localhost:8080/health`
   - Create a task: `curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title":"Demo Task","description":"Test the API"}'`
   - Retrieve the task: `curl http://localhost:8080/tasks/{id}`
   - Test error handling: `curl http://localhost:8080/tasks/invalid-id` (expect 404)
   - Verify all endpoints work as documented

### Success Criteria
- ✅ All acceptance criteria met
- ✅ All tests passing
- ✅ Checkstyle validation passing
- ✅ Human approval gate passed
- ✅ CI pipeline green
- ✅ Docker container runs successfully via setup.sh
- ✅ API endpoints respond correctly
- ✅ Documentation complete

---

## Why This Feature?

This task management feature is ideal for demonstrating agentic SDLC because it:

1. **Bounded Scope:** Only 2 endpoints, ~13 files, can be completed in a single session
2. **Real Patterns:** Shows common REST API patterns (CRUD, validation, error handling)
3. **Testable:** Clear inputs/outputs make testing straightforward
4. **Demonstrable:** Easy to show working functionality via curl commands
5. **No External Dependencies:** In-memory storage keeps setup simple
6. **Incremental:** Can be built layer by layer (model → repo → service → controller)
7. **Quality Gates:** Exercises all existing quality checks (tests, checkstyle, CI)
8. **Production-Like:** Follows proper layered architecture and best practices

---

## Next Steps

1. Create formal spec in `.kiro/specs/task-management/`
2. Begin implementation following the spec-driven workflow
3. Execute demo steps to validate end-to-end functionality
