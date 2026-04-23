# XJTUhub Backend

Spring Boot modular monolith backend.

## Current Scope

The current backend skeleton includes:

- Spring Boot application entrypoint.
- Global API response envelope shape.
- `requestId` and `durationMs` support.
- Health endpoint at `GET /api/v1/health`.
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
```

## Contract Source

Backend implementation must follow:

- `../doc/backend/prd/global-foundation.md`
- `../doc/backend/prd/global-api-contracts.md`
- `../doc/backend/prd/global-data-model.md`
- `../doc/backend/prd/global-auth-organization-permission.md`
