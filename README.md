# TalentSwipe

A social learning app for finding study partners, joining communities, sharing posts, and chatting with matches. Built with Java 21, Spring Boot 3, Vue 3, and MySQL.

The source uses the earlier project name **Biblo**, including the backend directory and Java package names.

## Features

- Profiles with skills, interests, projects, and experience.
- Swipe-based discovery and mutual-like matching.
- Community membership, post feeds, comments, and likes.
- JWT authentication, chat rooms, and media uploads.
- Seeded demo content and configurable bot activity for local development.

## Run locally

You need Java 21, Node.js 20+, and MySQL 8. The backend includes a Maven wrapper.

### Backend

1. Open a terminal in `biblov1/`.
2. Copy `.env.example` to `.env.local`.
3. Set your database credentials and replace `APP_JWT_SECRET` with a unique Base64-encoded key of at least 32 random bytes. For example: `openssl rand -base64 32`.
4. Run `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`).

The API runs at http://localhost:8080. The application loads `.env.local` from its working directory; OS environment variables take precedence. Hibernate updates the local schema on startup. Uploads are written relative to that working directory unless `UPLOAD_DIR` is set.

### Frontend

Open a second terminal in `frontend/`:

```sh
npm ci
npm run dev
```

Open http://localhost:5173. Copy `frontend/.env.example` to `frontend/.env.local` if you need to change `VITE_API_BASE_URL`.

## Repository layout

| Directory | Purpose |
| --- | --- |
| `biblov1/` | Spring Boot API, persistence, configuration, and integration tests |
| `frontend/` | Vue application, styles, and Playwright tests |
| `uploads/`, `biblov1/uploads/` | Existing development media |

## Checks

Run from `biblov1/`:

```sh
./mvnw test
```

Run from `frontend/`:

```sh
npm run build
npm run lint
npx playwright install
npm run test:e2e
```

See [TESTING.md](TESTING.md) for backend test configuration. The integration tests use an in-memory H2 database.

## Development notes

This is a development project. Review authentication, upload handling, logging, CORS, and database migration strategy before deployment. Demo bot activity is enabled by default; set `APP_BOTS_SOCIAL_ENABLED=false` to disable it.

Keep local credentials out of Git. Previously committed credentials must be rotated even after their files are removed.
