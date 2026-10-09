# Job Portal

Full-stack job portal: recruiters post jobs, candidates search and apply with a resume.

**Stack:** Java 17, Spring Boot 3, Spring Security (JWT), JPA/Hibernate, MySQL 8, React (Vite), Docker, Swagger/OpenAPI, JUnit 5 + Mockito.

## Features
- JWT auth with two roles: `RECRUITER` and `CANDIDATE` (method-level `@PreAuthorize`)
- Recruiters: create / update / delete jobs, view applicants, shortlist or reject, download resumes
- Candidates: search jobs (keyword, location, job type) with pagination, apply with resume upload (PDF/DOC/DOCX, max 5 MB), track status
- Application status: `APPLIED` → `SHORTLISTED` / `REJECTED`
- One application per candidate per job (DB unique constraint + service check)
- Resume access restricted to the owning candidate and the job's recruiter
- Consistent JSON errors via `@RestControllerAdvice`

## Run with Docker
```bash
docker compose up --build
```
- Frontend: http://localhost:3000
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Run locally
```bash
# MySQL (or: docker compose up mysql)
cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev   # http://localhost:5173
```

## Tests
```bash
cd backend && mvn test
```

## API overview
| Method | Path | Role |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/api/jobs?keyword=&location=&jobType=&page=&size=` | public |
| GET | `/api/jobs/{id}` | public |
| POST / PUT / DELETE | `/api/jobs`, `/api/jobs/{id}` | RECRUITER |
| GET | `/api/recruiter/jobs` | RECRUITER |
| POST | `/api/jobs/{id}/apply` (multipart `resume`) | CANDIDATE |
| GET | `/api/applications/mine` | CANDIDATE |
| GET | `/api/jobs/{id}/applications?status=` | RECRUITER (owner) |
| PATCH | `/api/applications/{id}/status` | RECRUITER (owner) |
| GET | `/api/applications/{id}/resume` | candidate or recruiter of that application |

## Project structure
```
backend/src/main/java/com/jobportal
  config/ controller/ dto/ entity/ exception/ repository/ security/ service/
frontend/src  (App, api.js, components/)
```
