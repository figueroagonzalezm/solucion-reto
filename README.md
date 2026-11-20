# Full Stack Exam — Clients

Full stack application to manage clients:

- **`com.exam.test`** — REST API (Spring Boot 3.0.6, Java 17, PostgreSQL, Swagger/OpenAPI).
- **`my-app-exam`** — Frontend (Angular 13 + Angular Material).

See [`SOLUTION.md`](SOLUTION.md) for what was implemented and the technical decisions.

## Requirements

| Tool | Version |
|------|---------|
| JDK | **17** |
| Node.js / npm | 16 or newer (tested with Node 24) |
| PostgreSQL | 12 or newer (tested with 16) |

> **Use JDK 17 to build the API.** Newer JDKs (23+) disable implicit annotation processing,
> which breaks Lombok with the version managed by Spring Boot 3.0.x. Make sure `JAVA_HOME`
> points to a JDK 17 before running Maven:
>
> ```bash
> # Windows (cmd)
> set JAVA_HOME=C:\path\to\jdk-17
> # Linux / macOS / Git Bash
> export JAVA_HOME=/path/to/jdk-17
> ```

## 1. Database

The API expects a PostgreSQL database named `db_test` with user `test` / password `test`
(defaults, see [Configuration](#configuration)).

### Option A — Docker

```bash
docker run -d --name exam-postgres \
  -e POSTGRES_DB=db_test -e POSTGRES_USER=test -e POSTGRES_PASSWORD=test \
  -p 5432:5432 postgres:16
```

### Option B — Local PostgreSQL

```sql
CREATE USER test WITH PASSWORD 'test';
CREATE DATABASE db_test OWNER test;
```

### Create the table

Run [`com.exam.test/db/init.sql`](com.exam.test/db/init.sql) against `db_test`:

```bash
# Docker
docker exec -i exam-postgres psql -U test -d db_test < com.exam.test/db/init.sql

# Local psql
psql -U test -d db_test -f com.exam.test/db/init.sql
```

The script is idempotent: it creates the `clients` table if missing and adds the `email`
column to databases created before it existed.

## 2. API (Spring Boot)

```bash
cd com.exam.test
./mvnw spring-boot:run          # Windows cmd: mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Build and run the tests (no database needed, see [Tests](#tests)):

```bash
./mvnw clean package
```

### Endpoints

| Method | Path | Description | Responses |
|--------|------|-------------|-----------|
| `GET` | `/api/clients` | List all clients | `200` |
| `POST` | `/api/client` | Create a client | `200` |
| `DELETE` | `/api/clients/{id}` | Delete a client | `204` deleted, `404` not found |

### Error format

All errors are handled in one place (`GlobalExceptionHandler`, a `@RestControllerAdvice`) and
return the same JSON body:

```json
{
  "timestamp": "2026-11-20T15:32:15.955Z",
  "status": 404,
  "error": "Not Found",
  "message": "Client with id 99999 not found",
  "path": "/api/clients/99999"
}
```

| Status | When |
|--------|------|
| `400` | Malformed or missing JSON body, or a non-numeric `{id}` |
| `404` | The client does not exist |
| `409` | The data violates a database constraint (e.g. a value longer than the column) |
| `500` | Unexpected or database error (details are logged, never returned) |

Example:

```bash
curl -X POST http://localhost:8080/api/client \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ann","lastName":"Lee","phone":"5551234","email":"ann@example.com"}'

curl http://localhost:8080/api/clients

curl -i -X DELETE http://localhost:8080/api/clients/1
```

### Configuration

Database settings are read from environment variables, with these defaults
(`com.exam.test/src/main/resources/application.properties`):

| Variable | Default |
|----------|---------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/db_test` |
| `DB_USER` | `test` |
| `DB_PASSWORD` | `test` |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |

`APP_CORS_ALLOWED_ORIGINS` is a comma-separated list of browser origins allowed to call `/api/**`
(property `app.cors.allowed-origins`), e.g. `https://app.example.com,https://admin.example.com`.
Swagger UI is served by the API itself (same origin), so it does not need to be listed.

Data access uses Spring Data JPA (Hibernate). The schema is created by `db/init.sql`, not by
Hibernate: the API runs with `spring.jpa.hibernate.ddl-auto=validate`, so it **fails at startup
if the `clients` table is missing or does not match the entity** (run `init.sql` first).

## 3. Frontend (Angular)

```bash
cd my-app-exam
npm install
npm start                       # same as: npx ng serve
```

Open **http://localhost:4200**. The API URL is set in
[`my-app-exam/src/environments/environment.ts`](my-app-exam/src/environments/environment.ts)
(`apiUrl`, default `http://localhost:8080/api`). The API allows CORS from `http://localhost:4200` by default.

The production build (`environment.prod.ts`) uses the relative URL `/api`, so it calls the API on the
host that serves the app (for example behind a reverse proxy). If the frontend is hosted on a different
origin than the API, set `apiUrl` to the API's full URL and add the frontend's origin to
`APP_CORS_ALLOWED_ORIGINS`.

### Client form

- Required fields: First Name, Last Name, Phone, Email. Missing fields are listed in an
  error message and highlighted in red when saving.
- Phone accepts digits only.
- Email must match the `user@domain.com` format.
- The form is cleared after a successful save.
- Each row of the table has a **Delete** button (calls `DELETE /api/clients/{id}`).

## Tests

### API (JUnit 5 + Mockito + MockMvc)

```bash
cd com.exam.test
./mvnw test
```

- `ClientControllerTest` (`@WebMvcTest`): every endpoint and error mapping of
  `GlobalExceptionHandler` (200, 204, 400, 404, 409, 500). No database needed.
- `ClientServiceTest`: entity/DTO mapping (including `email`) and the not-found case on delete.
- `ClientRepositoryTest` (`@DataJpaTest`): the JPA mapping (save, find, delete) against the schema.
- `TestApplicationTests`: checks that the Spring context starts and that the entity matches the schema
  (`ddl-auto=validate`).

**The API tests do not need PostgreSQL.** They run on in-memory H2 in PostgreSQL mode
(`src/test/resources/application.properties`), which loads the same `db/init.sql` used for
PostgreSQL, so a mismatch between the entity and the schema still fails the tests. PostgreSQL is only
used when you run the API (`src/main/resources/application.properties`).

### Frontend (Jasmine + Karma)

```bash
cd my-app-exam
npm run test:ci                 # one run in headless Chrome (needs Chrome installed)
npm test                        # watch mode in a Chrome window
```

- `registration.component.spec.ts`: the 7 form requirements (required-field message and red
  fields, form reset after saving, email field, numeric phone, email format, delete button).
- `client.service.spec.ts`: URLs and HTTP verbs for list, create and delete, plus API error messages.
- `app.component.spec.ts`: the app shell.

If Chrome is not on the default path, set `CHROME_BIN` to its executable.

## Project structure

```
.
├── com.exam.test/          # Spring Boot API
│   ├── db/init.sql         # PostgreSQL schema
│   └── src/main/java/com/exam/test/
│       ├── config/         # OpenAPI configuration
│       ├── controller/     # REST controller
│       ├── service/        # Business logic
│       ├── respository/    # Spring Data JPA repository
│       └── model/          # JPA entity and DTOs
├── my-app-exam/            # Angular app
│   └── src/app/
│       ├── components/registration/   # Client form and table
│       ├── services/client.service.ts # HTTP client for the API
│       └── interfaces/client.ts
├── ExamDatabaseObjects.sql # Original SQL Server script (reference only)
├── README.md
└── SOLUTION.md
```
