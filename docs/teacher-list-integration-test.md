# Teacher List Integration Verification

## Scope

This verification covers the teacher list from PostgreSQL and Flyway migrations through the REST API and the Vue page, including pagination, error responses, OpenAPI and the Docker Compose environment.

## Test scenarios and results

| Scenario | Verification | Result |
|---|---|---|
| Development seed data | `GET /api/v1/teachers?page=0&size=100` through the frontend proxy | Passed: 15 teachers returned from the `dev` profile. |
| Empty teacher table | `TeacherRepositoryTest.emptyTeacherTableReturnsEmptyPage` against a fresh PostgreSQL Testcontainer | Passed: empty content and zero totals. |
| Multiple pages | Repository pagination test and API requests with `size=2` | Passed: 8 pages for the seed data; consecutive pages contain different records. |
| No current primary employment | Repository query test and seed-data API response | Passed: teacher is included with a null unit and position. |
| Inactive teacher | Repository query test and seed-data API response | Passed: inactive record is returned with `active=false`. |
| Invalid list parameters | `size=101`, invalid sort field and service tests | Passed: HTTP 400, `VALIDATION_ERROR`, safe detail. |
| Unknown address | `GET /api/v1/unknown` | Passed: HTTP 404, `NOT_FOUND`, safe detail. |
| Unsupported method | `POST /api/v1/teachers` | Passed: HTTP 405, `METHOD_NOT_ALLOWED`. |
| Backend/database failure | Exception-handler tests simulate database failure; outages are checked through the Compose proxy | Passed: stopped database returns HTTP 503 with `SERVICE_UNAVAILABLE` in about 5 seconds; stopped backend returns HTTP 504, mapped to the frontend connection-error message. |
| Error response confidentiality | Controller and exception-handler tests check safe messages and absence of exception/database details | Passed. |
| Error ID correlation | Error response ID checked against backend logs | Passed: the response `errorId` is logged with its error code. |
| OpenAPI and Swagger UI | `/v3/api-docs` and `/swagger-ui/index.html` on the backend | Passed: teacher endpoint documents the shared Problem Details schema and error responses. |
| Fresh schema and entity validation | PostgreSQL Testcontainers run Flyway migrations and start Hibernate with `ddl-auto=validate` | Passed. |
| Compose startup and routing | `docker compose up --build -d`, `/teachers`, `/api/v1/teachers` | Passed: database, backend and frontend start; SPA refresh and proxied API return HTTP 200. |

## Findings and resolutions

| Finding | How it was found | Resolution | Related task |
|---|---|---|---|
| Invalid list parameters could expose user-supplied values in the 400 response detail. | Reviewing validation responses while checking error-response confidentiality. | Return a safe validation detail while retaining diagnostic information in backend logs, correlated by `errorId`. | SF-20 (error handling); verified in SF-29. |
| Requests could wait too long when the database or backend was unavailable. | Stopping the database and backend during the Docker Compose outage checks. | Limit database connection waits and configure proxy connection/read timeouts; database outages return 503 and backend outages return 504. | SF-20 (error handling); verified in SF-29. |
| Gateway responses 502/504 were not classified as connection errors in the frontend. | Stopping the backend during the Compose proxy check and observing frontend error handling. | Convert 502/504 responses to the frontend connection-error type and message. | SF-26 (frontend); verified in SF-29. |
| The OpenAPI response documentation did not fully describe the shared API error body and endpoint error responses. | Comparing `/v3/api-docs` with the actual error responses and error catalogue. | Document the shared Problem Details schema and the teacher endpoint's error responses. | SF-22 (OpenAPI); verified in SF-29. |

## Confluence handoff

Suggested page title: **SF-29 Teacher List Integration Verification**

Summary for publication: The teacher-list integration scenarios passed across PostgreSQL, the REST API, the frontend and Docker Compose. Verification uncovered unsafe validation details, excessive waits during database/backend outages, incorrect frontend handling of gateway timeouts, and incomplete OpenAPI error documentation. These findings were resolved under SF-20, SF-22 and SF-26 respectively and verified as part of SF-29.

**Publication status:** This report is ready to link or summarize on Confluence, but publication remains pending. Publish it before moving SF-29 to Done.

## Error catalogue and frontend messages

| API code | HTTP status | User-facing message |
|---|---:|---|
| `VALIDATION_ERROR` | 400 | Niepoprawne parametry zapytania |
| `MALFORMED_REQUEST` | 400 | Błąd składni zapytania |
| `NOT_FOUND` | 404 | Nie znaleziono zasobu |
| `METHOD_NOT_ALLOWED` | 405 | Niedozwolona metoda zapytania |
| `CONFLICT` | 409 | Konflikt stanu serwera |
| `SERVICE_UNAVAILABLE` | 503 | Serwis niedostępny |
| `INTERNAL_ERROR` | 500 | Błąd serwera |
| `UNAUTHENTICATED` | 401 | Wymagane jest zalogowanie |
| `ACCESS_DENIED` | 403 | Brak uprawnień do wykonania tej operacji |

Network failures use the frontend-only `FETCH_FAILED` code and display `Brak połączenia z serwerem`. API errors use RFC 9457 `application/problem+json` with `code`, `timestamp`, `errorId` and optional field errors. Internal exception text is logged under `errorId`, not returned as the response detail.

## Repeatable commands

From the repository root:

```sh
docker compose up --build -d
curl -i http://localhost/teachers
curl -i 'http://localhost/api/v1/teachers?page=0&size=2'
curl -i 'http://localhost/api/v1/teachers?size=101'
curl -i http://localhost/api/v1/unknown
curl -i -X POST http://localhost/api/v1/teachers
curl -i http://localhost:8080/v3/api-docs
```

Inspect the matching log entry with:

```sh
docker compose logs backend | grep '<errorId from the response>'
```

The backend tests use PostgreSQL Testcontainers. In Docker-based development environments where the test JVM is itself containerized, run the suite with host networking, `TESTCONTAINERS_RYUK_DISABLED=true`, and `TESTCONTAINERS_HOST_OVERRIDE=127.0.0.1`.