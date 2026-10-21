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
