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

# Backend database

The backend requires a PostgreSQL database. Connection parameters are passed through
environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`), the same ones `compose.yaml` provides in Docker.
For a local run, optionally keep them in `backend/.env` (see `backend/.env.example`).

Full setup guide: [backend/docs/database-setup.md](backend/docs/database-setup.md)

Check the database connection of a running backend with:

```bash
curl http://localhost:8080/actuator/health
```

# Frontend
The frontend is a Vue 3 app built with Vite and TypeScript.
To install dependencies, run `npm i` in the frontend directory.
To run the development server, run `npm run dev` in the frontend directory.

Vue Router with file-based routing is used for navigation. Pinia Colada is used for data fetching.

The API base URL is set by the Vite configuration, depending on the mode, and can be overridden via the `TAS_API_URL` environment variable.

# Local Environment Setup (Docker Compose)

The environment consists of a PostgreSQL database, a Spring Boot backend, and a Vue.js frontend.

### Prerequisites
- Docker and the Docker Compose plugin installed.

### Running the environment
1. Copy the example environment variables file:
   `cp .env.example .env`
2. Adjust the credentials in the `.env` file if necessary.
3. Build and run the containers in the background:
   `docker compose up --build -d`

Once successfully started, the services are available at:
- Frontend: http://localhost
- Backend API: http://localhost:8080
- Database: localhost:5432

### Stopping the environment
To stop the containers without removing them (database data is preserved):
`docker compose stop`

To stop and remove the containers (database volume data is preserved):
`docker compose down`

To stop, remove the containers, and completely wipe the database data (environment reset):
`docker compose down -v`
