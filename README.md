# Internship Inventory (Staj Envanter)

A web application for managing company internships end to end: interns, supervisors,
departments, internship periods, attendance, task templates, evaluations and IT requests.

The domain language of the codebase is Turkish (e.g. *stajyer* = intern, *sorumlu* =
supervisor, *devam* = attendance, *şablon* = template, *talep* = request).

## Structure

| Folder | Stack |
|:--|:--|
| `backend/` | Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL, Flyway |
| `frontend/` | React, TypeScript, Vite, MUI |

## Features

- Role-based access: **Admin**, **Supervisor**, **Intern**
- Account provisioning with one-time random temporary passwords and forced password change on first login
- Internship lifecycle: create, update, soft delete, department transfer, primary/backup supervisor
- Attendance tracking with supervisor approval
- Task templates assigned to internships, with per-item progress and approval
- Intern evaluation and scoring
- Upcoming end-date notifications (scheduled job, optional e-mail)
- IT request module: admins create requests, supervisors see their department's requests and assign them to interns
- Server-side pagination and Turkish-aware case-insensitive search

## Running locally

### Backend

Requires a running PostgreSQL instance.

Secrets are **not** stored in the repository. Provide them via environment variables
or a git-ignored `backend/application-local.yml`:

```bash
cd backend
cp application-local.yml.example application-local.yml   # then fill in the values
./mvnw spring-boot:run
```

| Variable | Required | Default |
|:--|:--|:--|
| `DB_URL` | no | `jdbc:postgresql://localhost:5432/postgres` |
| `DB_USERNAME` | no | `postgres` |
| `DB_PASSWORD` | **yes** | – |
| `APP_JWT_SECRET` | **yes** (min. 32 bytes, e.g. `openssl rand -base64 48`) | – |
| `ADMIN_EMAIL` | no | `admin@staj.local` |
| `ADMIN_SIFRE` | **yes** – password of the initial admin account | – |

The API listens on `http://localhost:8070`. Database schema is managed by Flyway
migrations in `backend/src/main/resources/db/migration`.

A Postman collection covering all endpoints is in `backend/postman/`.

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

Set `VITE_API_BASE_URL` (see `frontend/.env.example`) if the backend is not on
`http://localhost:8070`.
