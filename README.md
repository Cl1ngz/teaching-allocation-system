# Workflow

## Git Workflow

**Branching Structure:**

* **`main` (Production):** The holy grail. It only accepts code at the end of a sprint (via a Pull Request from `dev`) or for emergency hotfixes.
* **`dev` (Staging/Integration):** The active sprint branch. This is where all feature branches meet and are tested.
* **`feat/*` (Development):** Short-lived branches where the actual coding happens.

## **IMPORTANT: BRANCH OFF `dev`, NOT `main**`
Before starting a new feature, ensure you are pulling the latest code from the integration branch:
```bash
git checkout dev
git pull origin dev
git checkout -b feat/your-feature-name
```

## Commits name
`feat(TICKET-ID): description`

## Definition of Done
Before merging a Pull Request to `dev`, ensure:
- The code builds and automated tests pass.
- All acceptance criteria from the ticket are met.
- Documentation (README, setup and configuration guides) is updated to reflect any changes in behavior, endpoints, or configuration.
- Changes are reviewed and merged into `dev` via Pull Request.

# Backend database

The backend requires a PostgreSQL database. Connection parameters are passed through
environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`), the same ones `compose.yaml` provides in Docker.
For a local run, optionally keep them in `backend/.env` (see `backend/.env.example`).
Spring loads it automatically whether started from `backend/` or the repository root.

Full setup guide: [backend/docs/database-setup.md](backend/docs/database-setup.md)

Teacher-list integration scenarios and verification results:
[docs/teacher-list-integration-test.md](docs/teacher-list-integration-test.md)

Check the database connection of a running backend with:

```bash
curl http://localhost:8080/actuator/health
```

# Frontend

The frontend is a Vue 3 app built with Vite and TypeScript.
To install dependencies, run `npm i` in the frontend directory.
To run the development server, run `npm run dev` in the frontend directory.

Vue Router with file-based routing is used for navigation. Pinia Colada is used for data fetching.

By default, the frontend sends API requests to the relative `/api` path.
In development mode (`npm run dev`, available at `http://localhost:5173`), Vite proxies `/api` requests to the backend (default: `http://localhost:8080`).

To use a different backend address in development, override the `TAS_BACKEND_URL` environment variable (in `frontend/.env` or inline):

```bash
TAS_BACKEND_URL=http://localhost:8081 npm run dev
```

# Local Environment Setup (Docker Compose)

The environment consists of a PostgreSQL database, a Spring Boot backend, and a Vue.js frontend served by Nginx (which also reverse-proxies `/api` requests to the backend).

### Prerequisites

- Docker and the Docker Compose plugin installed.

### Running the environment

1. Copy the example environment variables file:
   ```bash
   cp .env.example .env
   ```
2. Adjust the credentials in the `.env` file if necessary.
3. Build and run the containers in the background:
   ```bash
   docker compose up --build -d
   ```

Once successfully started, the services are available at:

- **Application (Frontend):** `http://localhost`
- **API (via Nginx reverse proxy):** `http://localhost/api/...`
- **Backend (direct, for diagnostics):** `http://localhost:8080`
- **Database:** `localhost:5432`

### Stopping the environment

To stop the containers without removing them (database data is preserved):

```bash
docker compose stop
```

To stop and remove the containers (database volume data is preserved):

```bash
docker compose down
```

To stop, remove the containers, and completely wipe the database data (environment reset):

```bash
docker compose down -v
```
