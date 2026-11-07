# Teaching Allocation System - Backend

Backend service for the Teaching Allocation System built with Spring Boot and Java 21.

## Prerequisites

- Java 21 (JDK 21)
- Maven wrapper is included (`./mvnw`)

## Running the Application

To start the backend application locally:

```bash
./mvnw spring-boot:run
```

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

The application runs on `http://localhost:8080`.

## Running Tests

To run the automated test suite:

```bash
./mvnw test
```

## API Endpoints

### 1. Hello Endpoint (System Verification)

Basic diagnostic endpoint to verify backend operation and HTTP connectivity between the frontend and server.

- **URL:** `/api/hello`
- **Method:** `GET`
- **Authentication:** None (Public)
- **Response HTTP Code:** `200 OK`
- **Response Format:** `application/json`
- **Response Body Example:**

```json
{
  "message": "Teaching Allocation System API"
}
```

**Curl Example:**

```bash
curl -i http://localhost:8080/api/hello
```

### 2. Health Endpoint

System health check endpoint.

- **URL:** `/api/v1/health`
- **Method:** `GET`
- **Authentication:** None (Public)
- **Response HTTP Code:** `200 OK`
- **Response Format:** `application/json`

**Curl Example:**

```bash
curl -i http://localhost:8080/api/v1/health
```
