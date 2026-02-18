# Task Management API - Executable Implementation Plan

## File-by-File Change Plan

### Branch A: Backend + Tests (11 files)

#### New Source Files (7 files)

**1. `src/main/java/com/kiro/sdls/model/TaskStatus.java`**
- Enum with values: PENDING, IN_PROGRESS, COMPLETED
- Simple enum, no additional logic

**2. `src/main/java/com/kiro/sdls/model/Task.java`**
- Fields: id (UUID), title (String), description (String), status (TaskStatus), createdAt (LocalDateTime)
- Bean Validation annotations: @NotBlank on title, @Size constraints
- Constructor, getters, setters

**3. `src/main/java/com/kiro/sdls/repository/TaskRepository.java`**
- @Repository annotation
- ConcurrentHashMap<UUID, Task> for in-memory storage
- Methods: save(Task), findById(UUID) returning Optional<Task>

**4. `src/main/java/com/kiro/sdls/service/TaskService.java`**
- @Service annotation
- Inject TaskRepository
- Methods: createTask(title, description), getTaskById(id)
- Business logic: generate UUID, set status to PENDING, set timestamp

**5. `src/main/java/com/kiro/sdls/controller/TaskController.java`**
- @RestController with @RequestMapping("/tasks")
- Inject TaskService
- POST / endpoint: accepts CreateTaskRequest, returns 201 with Task
- GET /{id} endpoint: returns 200 with Task or 404

**6. `src/main/java/com/kiro/sdls/dto/CreateTaskRequest.java`**
- Fields: title (String), description (String)
- Bean Validation annotations matching Task validation rules

**7. `src/main/java/com/kiro/sdls/exception/GlobalExceptionHandler.java`**
- @RestControllerAdvice annotation
- Handle MethodArgumentNotValidException (return 400 with error details)
- Handle NoSuchElementException (return 404 with error message)

#### Test Files (3 files)

**8. `src/test/java/com/kiro/sdls/service/TaskServiceTest.java`**
- Test createTask: validates UUID generation, status=PENDING, timestamp set
- Test getTaskById: validates retrieval and exception on not found

**9. `src/test/java/com/kiro/sdls/controller/TaskControllerTest.java`**
- @SpringBootTest + @AutoConfigureMockMvc
- Test POST /tasks: valid request returns 201
- Test GET /tasks/{id}: found returns 200, not found returns 404
- Test validation: empty title returns 400
- Test validation: whitespace-only title returns 400
- Test validation: title with 201 characters returns 400
- Test validation: title with 200 characters returns 201 (boundary valid)
- Test validation: description with 1001 characters returns 400
- Test validation: description with 1000 characters returns 201 (boundary valid)

**10. `src/test/java/com/kiro/sdls/repository/TaskRepositoryTest.java`**
- Test save and findById operations
- Test findById returns empty Optional when not found

#### Modified Files (1 file)

**11. `pom.xml`**
- Add spring-boot-starter-validation dependency (if not already present)
- No other changes needed

---

### Branch B: Infrastructure + Documentation (3 files)

#### Modified Files (3 files)

**12. `setup.sh`**
- Keep existing health check logic
- Add validation for POST /tasks (create a task, capture task ID from response)
- Add validation for GET /tasks/{id} (retrieve the created task)
- Exit with SUCCESS only if all three checks pass

**13. `README.md`**
- Add Task Management API section after health endpoint
- Document POST /tasks with request/response examples
- Document GET /tasks/{id} with request/response examples
- Add curl examples for local testing
- Update project structure diagram

**14. `docker-compose.yml`**
- No changes required (existing healthcheck on /health is sufficient)
- Document: verify this file doesn't need changes

---

## Total Files: 14 files
- 7 new source files
- 3 new test files
- 1 modified source file (pom.xml)
- 3 modified infrastructure files (setup.sh, README.md, docker-compose.yml)

**Status: Under 20 file limit ✅**

---

## Branch Strategy

### Branch A: `feature/task-api-backend`
**Purpose:** Core backend implementation and tests

**Files:**
- All source files (model, repository, service, controller, dto, exception)
- All test files
- pom.xml

**Dependencies:** None (can start immediately)

**Merge Order:** Merge first (provides working API)

---

### Branch B: `feature/task-api-infrastructure`
**Purpose:** Infrastructure validation and documentation

**Files:**
- setup.sh
- README.md
- docker-compose.yml (verify only)

**Dependencies:** Requires Branch A merged (needs working /tasks endpoints)

**Merge Order:** Merge second (validates Branch A works)

---

## Verification Commands

### Branch A Verification (Backend + Tests)

```bash
# 1. Run all tests
mvn clean test

# Expected: All tests pass, including new TaskService, TaskController, TaskRepository tests

# 2. Run quality checks
mvn verify

# Expected: BUILD SUCCESS, 0 checkstyle violations

# 3. Build the application
mvn clean package

# 4. Start application locally
java -jar target/*.jar &
APP_PID=$!
sleep 5

# 5. Test health endpoint (baseline)
curl -s http://localhost:8080/health
# Expected: {"status":"ok"}

# 6. Test POST /tasks
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Task","description":"Testing the API"}')
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
echo "HTTP Code: $HTTP_CODE"
echo "Body: $BODY"
# Expected: HTTP Code 201, Body contains id, title, description, status=PENDING, createdAt

# 7. Extract task ID and test GET /tasks/{id}
TASK_ID=$(echo "$BODY" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
curl -s http://localhost:8080/tasks/$TASK_ID
# Expected: Same task object as POST response

# 8. Test 404 error
curl -s -w "\nHTTP %{http_code}\n" http://localhost:8080/tasks/00000000-0000-0000-0000-000000000000
# Expected: HTTP 404 with error message

# 9. Test validation error (empty title)
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"","description":"test"}'
# Expected: HTTP 400 with validation error

# 10. Stop application
kill $APP_PID
wait $APP_PID 2>/dev/null || true

# 11. Build Docker image
docker build -t task-api:test .
# Expected: BUILD SUCCESS

# 12. Run Docker container
docker run -d -p 8080:8080 --name task-api-test task-api:test
sleep 10

# 13. Test in Docker
curl -s http://localhost:8080/health
curl -s -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Docker Test","description":"Testing in container"}'

# 14. Cleanup
docker stop task-api-test
docker rm task-api-test
```

**Branch A Success Criteria:**
- ✅ `mvn verify` passes
- ✅ All new tests pass (including boundary validation tests)
- ✅ POST /tasks returns 201 with valid task
- ✅ GET /tasks/{id} returns 200 with task
- ✅ GET /tasks/{invalid-id} returns 404
- ✅ POST /tasks with invalid data returns 400
- ✅ Docker build succeeds
- ✅ Endpoints work in Docker

---

### Branch B Verification (Infrastructure + Documentation)

**Prerequisites:** Branch A must be merged to main

```bash
# 1. Ensure latest main is pulled
git checkout main
git pull origin main

# 2. Checkout Branch B
git checkout feature/task-api-infrastructure

# 3. Run setup.sh (automated validation)
./setup.sh

# Expected output:
# Starting Docker Compose...
# Waiting for /health endpoint to return status ok (max 30 retries)...
# Attempt 1/30...
# Health check passed
# Testing POST /tasks...
# Task created with ID: <uuid>
# Testing GET /tasks/<uuid>...
# Task retrieved successfully
# SUCCESS

# 4. Manual verification (if setup.sh succeeds)
curl -s http://localhost:8080/health
# Expected: {"status":"ok"}

curl -s -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Manual Test","description":"Verifying setup"}'
# Expected: 201 with task object

# 5. Verify README documentation
cat README.md | grep -A 20 "Task Management"
# Expected: Documentation for POST /tasks and GET /tasks/{id} with examples

# 6. Cleanup
docker compose down
```

**Branch B Success Criteria:**
- ✅ setup.sh exits with SUCCESS (exit code 0)
- ✅ setup.sh validates /health endpoint
- ✅ setup.sh validates POST /tasks
- ✅ setup.sh validates GET /tasks/{id}
- ✅ README includes complete API documentation
- ✅ README includes curl examples

---

## PR Checklist

### Branch A PR: Backend Implementation

**Title:** `feat: Add Task Management API with POST and GET endpoints`

**Description must include:**

1. **Summary**
   - Added Task Management API with 2 endpoints
   - In-memory storage using ConcurrentHashMap
   - Bean validation for input
   - Global exception handling for 404 and 400 errors

2. **Files Changed**
   - List all 11 files (7 new source, 3 new tests, 1 modified pom.xml)

3. **Test Evidence**
   ```
   ✅ mvn verify: BUILD SUCCESS
   ✅ Checkstyle violations: 0
   ✅ Test results: X passed, 0 failed
   ✅ New test coverage: TaskService, TaskController, TaskRepository
   ```

4. **Manual Testing Evidence**
   ```
   ✅ POST /tasks returns 201 with valid task
   ✅ GET /tasks/{id} returns 200 with task
   ✅ GET /tasks/{invalid-id} returns 404
   ✅ POST /tasks with empty title returns 400
   ✅ Endpoints work in Docker container
   ```

5. **Acceptance Criteria Met**
   - [ ] Criterion 1: Task creation with validation (1-200 char title, 0-1000 char description)
   - [ ] Criterion 2: Task retrieval with 404 handling
   - [ ] Criterion 3: In-memory persistence with unique UUIDs
   - [ ] Criterion 4: Zero checkstyle violations
   - [ ] Criterion 5: All endpoints and methods tested
   - [ ] Criterion 6: CI/CD passes (mvn verify + Docker build)

6. **Screenshots/Logs**
   - Terminal output of `mvn verify`
   - curl command outputs showing 201, 200, 404, 400 responses

7. **Reviewer Notes**
   - No database required (in-memory only)
   - No breaking changes to existing /health endpoint
   - Ready for Branch B (infrastructure updates)

---

### Branch B PR: Infrastructure & Documentation

**Title:** `feat: Update setup.sh and README for Task Management API`

**Description must include:**

1. **Summary**
   - Updated setup.sh to validate /tasks endpoints
   - Added comprehensive API documentation to README
   - Verified docker-compose.yml requires no changes

2. **Files Changed**
   - setup.sh (enhanced validation)
   - README.md (API documentation)
   - docker-compose.yml (verified, no changes)

3. **Test Evidence**
   ```
   ✅ ./setup.sh exits with SUCCESS
   ✅ Health check passes
   ✅ POST /tasks validated in setup.sh
   ✅ GET /tasks/{id} validated in setup.sh
   ```

4. **Manual Testing Evidence**
   ```
   ✅ setup.sh completes in < 30 seconds
   ✅ All three validations pass (health, POST, GET)
   ✅ README examples are accurate and runnable
   ```

5. **Acceptance Criteria Met**
   - [ ] Criterion 7: setup.sh validates /health and /tasks endpoints
   - [ ] Criterion 7: Container serves both endpoint types
   - [ ] Criterion 8: README updated with examples

6. **Screenshots/Logs**
   - Terminal output of `./setup.sh` showing SUCCESS
   - Example curl commands from README executed successfully

7. **Reviewer Notes**
   - Depends on Branch A being merged
   - No changes to docker-compose.yml needed
   - setup.sh maintains backward compatibility

---

## Merge Order & Timeline

1. **Branch A** → Review → Merge to main (Human approval gate)
2. **Branch B** → Review → Merge to main (Human approval gate)
3. **Final validation:** Run `./setup.sh` on main branch
4. **Demo ready:** All acceptance criteria met

---

## Risk Mitigation

**Potential Issues:**

1. **Checkstyle violations on new files**
   - Mitigation: Run `mvn verify` frequently during development
   - Check line length (120 max), imports, naming conventions

2. **Bean validation dependency missing**
   - Mitigation: Verify spring-boot-starter-validation in pom.xml
   - Spring Boot 3.3.0 should include it by default

3. **setup.sh parsing JSON response**
   - Mitigation: Use simple grep/cut commands, avoid jq dependency
   - Test extraction logic before full implementation

4. **Docker build time in CI**
   - Mitigation: Existing CI already builds Docker, no new overhead
   - Multi-stage build keeps image size small

5. **Concurrent access to ConcurrentHashMap**
   - Mitigation: ConcurrentHashMap handles thread safety
   - No additional synchronization needed for this PoC

---

## Success Metrics

**Definition of Done:**

- ✅ All 14 files created/modified as planned
- ✅ `mvn verify` passes with 0 violations
- ✅ All tests pass (unit + integration)
- ✅ `./setup.sh` exits with SUCCESS
- ✅ Both PRs approved and merged
- ✅ All 9 acceptance criteria met
- ✅ Demo can be completed in 10-12 minutes
- ✅ No breaking changes to existing functionality

**Rollback Plan:**

If issues arise:
1. Revert Branch B (infrastructure) - no code impact
2. Revert Branch A (backend) - returns to baseline
3. Both branches are independent and safe to revert
