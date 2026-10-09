# 5-Day Commit Plan

Do each day's commits on that day (not all at once) so the history reflects real work.
Every commit below leaves the project compiling.

```bash
git init -b main
BE=backend/src/main/java/com/jobportal
BT=backend/src/test/java/com/jobportal
```

---
## Day 1: Project setup and data layer
```bash
git add .gitignore backend/pom.xml backend/src/main/resources/application.yml $BE/JobPortalApplication.java
git commit -m "chore: initialize Spring Boot project with MySQL, JPA and security dependencies"

git add $BE/entity/User.java $BE/entity/Role.java $BE/repository/UserRepository.java
git commit -m "feat: add User entity, Role enum and repository"

git add $BE/entity/Job.java $BE/entity/JobType.java $BE/repository/JobRepository.java
git commit -m "feat: add Job entity with search query (keyword, location, type)"

git add $BE/entity/Application.java $BE/entity/ApplicationStatus.java $BE/repository/ApplicationRepository.java
git commit -m "feat: add Application entity with status and unique job/candidate constraint"

git add $BE/exception
git commit -m "feat: add custom exceptions and global exception handler"
```

## Day 2: JWT authentication and roles
```bash
git add $BE/security/JwtUtil.java
git commit -m "feat: add JWT token generation and validation"

git add $BT/security/JwtUtilTest.java
git commit -m "test: add JwtUtil unit tests"

git add $BE/security/CustomUserDetailsService.java $BE/security/JwtAuthFilter.java
git commit -m "feat: add UserDetailsService and JWT authentication filter"

git add $BE/config/SecurityConfig.java
git commit -m "feat: configure stateless Spring Security with role-based method security"

git add $BE/dto/RegisterRequest.java $BE/dto/LoginRequest.java $BE/dto/AuthResponse.java $BE/service/AuthService.java
git commit -m "feat: add AuthService for register and login"

git add $BE/controller/AuthController.java
git commit -m "feat: add auth endpoints"

git add $BT/service/AuthServiceTest.java
git commit -m "test: add AuthService unit tests with Mockito"
```

## Day 3: Job APIs, pagination, search, Swagger
```bash
git add $BE/dto/JobRequest.java $BE/dto/JobResponse.java $BE/dto/PageResponse.java
git commit -m "feat: add job DTOs and generic page response"

git add $BE/service/JobService.java
git commit -m "feat: add JobService with CRUD, ownership checks, pagination and filtering"

git add $BE/controller/JobController.java
git commit -m "feat: add job endpoints (public search, recruiter-only create/update/delete)"

git add $BE/config/OpenApiConfig.java
git commit -m "docs: add Swagger/OpenAPI with JWT bearer auth"

git add $BT/service/JobServiceTest.java
git commit -m "test: add JobService unit tests"
```

## Day 4: Applications, resume upload, status tracking
```bash
git add $BE/service/FileStorageService.java
git commit -m "feat: add resume storage with type validation"

git add $BE/dto/ApplicationResponse.java $BE/dto/StatusUpdateRequest.java $BE/service/ApplicationService.java
git commit -m "feat: add ApplicationService (apply, list, shortlist/reject, resume access control)"

git add $BE/controller/ApplicationController.java
git commit -m "feat: add application endpoints"

git add $BT/service/ApplicationServiceTest.java
git commit -m "test: add ApplicationService unit tests"
```

## Day 5: Docker, simple React frontend, docs
```bash
git add backend/Dockerfile docker-compose.yml
git commit -m "chore: dockerize backend and add docker-compose with MySQL"

git add frontend/package.json frontend/vite.config.js frontend/index.html frontend/src/main.jsx frontend/src/api.js frontend/src/styles.css
git commit -m "feat(frontend): scaffold Vite React app with API client"

git add frontend/src/components/Auth.jsx frontend/src/components/Jobs.jsx
git commit -m "feat(frontend): add login/register and job search with pagination and apply"

git add frontend/src/components/MyApplications.jsx frontend/src/components/Recruiter.jsx
git commit -m "feat(frontend): add candidate applications and recruiter dashboard"

git add frontend/src/App.jsx
git commit -m "feat(frontend): wire up app shell with role-based navigation"

git add frontend/Dockerfile frontend/nginx.conf
git commit -m "chore: dockerize frontend with nginx API proxy"

git add README.md COMMIT_PLAN.md
git commit -m "docs: add README with setup, API overview and architecture"

git remote add origin https://github.com/<your-username>/job-portal.git
git push -u origin main
```
