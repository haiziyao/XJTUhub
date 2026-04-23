# XJTUhub Backend

Spring Boot modular monolith backend.

## Current Scope

The current backend skeleton includes:

- Spring Boot application entrypoint.
- Global API response envelope shape.
- Global exception handling for validation, business, and unexpected errors.
- Shared offset/cursor pagination DTOs.
- `requestId` and `durationMs` support.
- Health endpoint at `GET /api/v1/health`.
- Dependency health endpoint at `GET /api/v1/health/dependencies`.
- Auth/user MVP endpoints:
  - `POST /api/v1/auth/email-tokens`
  - `POST /api/v1/auth/email-sessions`
  - `GET /api/v1/auth/sessions`
  - `DELETE /api/v1/auth/sessions/current`
  - `GET /api/v1/users/me`
- Flyway migrations mirrored from `../database/mysql`.
- Environment variable placeholders for MySQL, Redis, and MinIO.

## Run Tests

```powershell
mvn.cmd test
```

## Local Configuration

Use `.env.local` for real local credentials. It is ignored by Git.

Use `.env.example` as the safe template.

Required variables:

```text
MYSQL_JDBC_URL
MYSQL_USERNAME
MYSQL_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
MINIO_ENDPOINT
MINIO_ACCESS_KEY
MINIO_SECRET_KEY
MINIO_BUCKET_NAME
FLYWAY_ENABLED
```

## Database Migration

Flyway is disabled by default to avoid accidental schema writes in ad-hoc local runs.

Set `FLYWAY_ENABLED=true` when the configured MySQL database should be migrated on application startup. Migration files live in `src/main/resources/db/migration` and must stay compatible with the authoritative bootstrap SQL in `../database/mysql`.

## Dependency Health

`GET /api/v1/health/dependencies` checks configured infrastructure:

- MySQL: `SELECT 1`
- Redis: `PING`
- MinIO: configured bucket existence

If a dependency client is not configured in the current runtime, its status is `skipped`.

## Contract Source

Backend implementation must follow:

- `../doc/backend/prd/global-foundation.md`
- `../doc/backend/prd/global-api-contracts.md`
- `../doc/backend/prd/global-data-model.md`
- `../doc/backend/prd/global-auth-organization-permission.md`
