# Biblo

Full-stack study/social platform built with Spring Boot 3 + MySQL backend and Vue 3 + Vite frontend.

## Features
- JWT-based auth (login, register, current user) with stateless security.
- Profiles with skills, interests, social links, and basic demographics.
- Communities: create/join/leave, membership counts, tagged discovery.
- Posts with hashtags, optional image uploads, comments, likes, per-community feeds.
- Swipe-based matching plus chat rooms/messages between matched users.
- File uploads served from `/uploads/**` with a 50MB limit.

## Project layout
- `biblov1/` Spring Boot API and services.
- `frontend/` Vue 3 single-page app.
- `uploads/` shared upload directory.

## Prerequisites
- Java 21 and Maven (wrapper included).
- Node.js 20+ and npm.
- MySQL 8+ running (default `jdbc:mysql://localhost:3306/biblov1`).
- Free ports: 8080 (API) and 5173 (web).

## Backend setup
```bash
cd biblov1
# Update src/main/resources/application.properties as needed:
# spring.datasource.url=jdbc:mysql://localhost:3306/biblov1
# spring.datasource.username=youruser
# spring.datasource.password=yourpass
# app.jwtSecret=changeme
# app.upload.dir=uploads
./mvnw spring-boot:run   # or mvnw.cmd on Windows
```
- Schema is managed by JPA (`spring.jpa.hibernate.ddl-auto=update`).
- Uploads save under `biblov1/uploads` and are served at `/uploads/**`.
- Run tests: `./mvnw test`.

## Frontend setup
```bash
cd frontend
npm install
npm run dev       # served at http://localhost:5173
npm run build     # production bundle
npm run preview   # preview build locally
```
- Axios base URL is configured in `src/main.js` from `VITE_API_BASE_URL` (fallback `http://localhost:8080`). For prod, set `VITE_API_BASE_URL` to your deployed backend (see `frontend/.env.example`).
- Lint: `npm run lint`.
- E2E: `npx playwright install` (first time), then `npm run test:e2e`.

## API quick start
Base URL: `http://localhost:8080/api`
- Auth: `POST /auth/register`, `POST /auth/login` (returns JWT), `GET /auth/me`.
- Communities: `GET /communities`, `POST /communities`, join/leave via `/communities/{id}/join` or `/communities/{id}/leave`, member count and membership checks.
- Posts: `POST /posts` (content + optional `imageFile`, `hashtags` JSON array string, `communityId`), `GET /posts/{id}`, `GET /posts/community/{communityId}`, update/delete endpoints.
- Comments/Likes, Skills, Matches/Swipes, Chat: controllers live in `biblov1/src/main/java/com/example/biblov1/controller/`.

## Deployment notes
- Externalize secrets (DB credentials, `app.jwtSecret`) instead of committing them to `application.properties`.
- CORS is open for development; lock down allowed origins for production.
- Adjust file upload path/limits in `application.properties` if deploying to shared storage.
