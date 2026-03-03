# Biblo

**Biblo** is a comprehensive full-stack application designed to connect learners and collaborators through social features, study communities, and real‑time communication. It combines a robust Spring Boot API with a modern Vue.js frontend to deliver a responsive and secure user experience.

---

## 🚀 Overview
Biblo addresses the needs of students and professionals looking to form study groups, share resources, and network with peers who have similar interests and skills. The platform supports:

- User profiles enriched with skills, interests, and social links.
- Creation and discovery of topic‑focused communities.
- Post feeds with hashtags, media attachments, and community scopes.
- A Tinder‑style swipe interface for matching study partners, with integrated chat rooms.
- Secure authentication and authorization using JSON Web Tokens (JWT).
- File upload capability for profile images and post attachments.

Architecturally, Biblo is divided into two main components:

1. **Backend (`biblov1/`)** – A Spring Boot 3 application powered by MySQL for persistence, exposing a RESTful API under `/api`.
2. **Frontend (`frontend/`)** – A Vue 3 single‑page application built with Vite, consuming the API and rendering a mobile‑friendly UI.

Uploads are stored in the top‑level `uploads/` directory and served statically by the backend.

---

## 🗂️ Project Structure
- `biblov1/` – Java source, resources, Maven wrapper, and server configuration.
- `frontend/` – Vue components, assets, build configuration, and tests.
- `uploads/` – Shared directory for user‑uploaded files (images, documents).

---

## 🛠️ Prerequisites
Before running the application, ensure your development environment includes:

- **Java 21** and **Maven** (the project includes the Maven wrapper).
- **Node.js 20+** with **npm**.
- **MySQL 8+** (default connection string `jdbc:mysql://localhost:3306/biblov1`).
- Available network ports: `8080` (API) and `5173` (frontend development server).

---

## 🧩 Backend Setup & Development
1. Navigate to the backend directory:
   ```bash
   cd biblov1
   ```
2. Configure database and application settings in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/biblov1
   spring.datasource.username=youruser
   spring.datasource.password=yourpass
   app.jwtSecret=changeme
   app.upload.dir=uploads
   ```
3. Start the application:
   ```bash
   ./mvnw spring-boot:run   # (or mvnw.cmd on Windows)
   ```

- The JPA provider handles schema migration (`spring.jpa.hibernate.ddl-auto=update`).
- Uploaded files save to `biblov1/uploads` and are accessible via `/uploads/**`.
- Execute unit and integration tests with `./mvnw test`.

---

## 🖥️ Frontend Setup & Development
1. Change into the frontend directory and install dependencies:
   ```bash
   cd frontend
   npm install
   ```
2. Run the development server:
   ```bash
   npm run dev    # available at http://localhost:5173
   ```
3. Build for production:
   ```bash
   npm run build
   npm run preview    # preview the production bundle locally
   ```

Important notes:
- API base URL is determined in `src/main.js` from the `VITE_API_BASE_URL` environment variable (default: `http://localhost:8080`).
- Example environment file: `frontend/.env.example`.
- Linting: `npm run lint`.
- End‑to‑end tests are powered by Playwright:
  ```bash
  npx playwright install   # first time only
  npm run test:e2e
  ```

---

## 📡 API Endpoints (Quick Reference)
Base URL: `http://localhost:8080/api`

| Area | Endpoint | Description |
|------|----------|-------------|
| Auth | `POST /auth/register` | Register new user |
|      | `POST /auth/login` | Authenticate (returns JWT) |
|      | `GET /auth/me` | Get current user info |
| Communities | `GET /communities` | List communities |
|             | `POST /communities` | Create a community |
|             | `/communities/{id}/join`<br>`/leave` | Manage membership |
| Posts       | `POST /posts` | Create post (supports `imageFile`, `hashtags`, `communityId`) |
|             | `GET /posts/{id}` | Retrieve post |
|             | `GET /posts/community/{communityId}` | Community feed |
| Others      | Comment, like, skill management, matching, chat controllers are located under `<backend>/src/main/java/com/example/biblov1/controller/` |

Refer to the source code for complete request/response schemas.

---

## 🛡️ Deployment & Production Considerations
- **Security**: Do not commit sensitive values (database credentials, `app.jwtSecret`) to version control. Use environment variables or a secrets manager.
- **CORS**: The current configuration allows all origins for development. Restrict it appropriately in production.
- **Uploads**: Adjust the upload directory and size limits (`spring.servlet.multipart.max-file-size`) if deploying to a cloud storage or shared filesystem.

---

## 🤝 Contributing
Contributions are welcome! Please fork the repository, create a feature branch, and submit a pull request with a clear description of changes.

---

## 📞 Support & Contact
For questions or feedback, open an issue or reach out to the maintainer at `maintainer@example.com`.


---

*Last updated: February 28, 2026.*
