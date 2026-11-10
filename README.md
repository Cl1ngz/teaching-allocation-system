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

# Frontend
The frontend is a Vue 3 app built with Vite and TypeScript.
To install dependencies, run `npm i` in the frontend directory.
To run the development server, run `npm run dev` in the frontend directory.

Vue Router with file-based routing is used for navigation. Pinia Colada is used for data fetching.

The API base URL is set by the Vite configuration, depending on the mode, and can be overridden via the `TAS_API_URL` environment variable.
