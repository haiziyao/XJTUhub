# Auth Module API

This document describes the currently implemented `auth` module APIs.

Base path:

```text
/api/v1/auth
```

Global response envelope, error handling, ID rules, and pagination rules inherit:

- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/prd/global-auth-organization-permission.md`

## 1. Create Email Token

```text
POST /api/v1/auth/email-tokens
```

Purpose:

- Create an email verification token for login.

Request:

```json
{
  "email": "student@example.com",
  "purpose": "login"
}
```

Current validation:

- `email` must be a valid email address.
- `purpose` must be non-empty.

Success response `data`:

```json
{
  "email": "student@example.com",
  "purpose": "login",
  "expiresAt": "2026-04-23T17:24:54.131Z",
  "delivery": "accepted",
  "token": null
}
```

Behavior notes:

- If `xjtuhub.auth.email.debug-return-token=true`, `delivery` becomes `debug_return` and `token` contains the raw token.
- Otherwise the module calls `EmailSender` and does not return the raw token.
- Creation is rate-limited per `email + purpose`.

Current errors:

- `VALIDATION_FAILED`
- `RATE_LIMITED`

## 2. Create Email Session

```text
POST /api/v1/auth/email-sessions
```

Purpose:

- Consume an email token and create an HttpOnly session cookie.

Request:

```json
{
  "email": "student@example.com",
  "purpose": "login",
  "token": "raw-token",
  "deviceLabel": "Chrome on Windows"
}
```

Success response `data`:

```json
{
  "user": {
    "id": "1776964323268002",
    "nickname": "student",
    "avatarUrl": null,
    "bio": null,
    "authLevel": "email_user",
    "nameColor": "default",
    "primaryIdentityProvider": "email",
    "lastLoginProvider": "email",
    "displayBadges": [
      {
        "type": "identity",
        "code": "email_verified",
        "label": "Email Verified",
        "tone": "neutral"
      }
    ]
  },
  "session": {
    "id": "1776964323376007",
    "loginProvider": "email",
    "deviceLabel": "Chrome on Windows",
    "createdAt": "2026-04-23T17:12:03.376Z",
    "expiresAt": "2026-05-23T17:12:03.376Z",
    "current": true
  }
}
```

Cookie behavior:

- Sets `XJTUHUB_SESSION`.
- Cookie is `HttpOnly`.
- Cookie path is `/`.
- Cookie uses `SameSite=Lax`.

Current errors:

- `VALIDATION_FAILED`
- `AUTH_EMAIL_TOKEN_INVALID`
- `AUTH_EMAIL_TOKEN_EXPIRED`
- `AUTH_EMAIL_TOKEN_CONSUMED`

## 3. List Sessions

```text
GET /api/v1/auth/sessions
```

Purpose:

- Return active sessions for the current user.

Success response `data`:

```json
{
  "items": [
    {
      "id": "1776964323376007",
      "loginProvider": "email",
      "deviceLabel": "Chrome on Windows",
      "createdAt": "2026-04-23T17:12:03.376Z",
      "expiresAt": "2026-05-23T17:12:03.376Z",
      "current": true
    }
  ],
  "page": 1,
  "pageSize": 1,
  "total": 1,
  "hasNext": false
}
```

Current errors:

- `AUTH_LOGIN_REQUIRED`

## 4. Revoke Current Session

```text
DELETE /api/v1/auth/sessions/current
```

Purpose:

- Revoke the current session and clear the session cookie.

Success response `data`:

```json
{
  "revoked": true
}
```

Current errors:

- `AUTH_LOGIN_REQUIRED`

## 5. Revoke Session By ID

```text
DELETE /api/v1/auth/sessions/{sessionId}
```

Purpose:

- Revoke another active session owned by the current user.

Rules:

- `sessionId` must be a numeric string.
- The target session must belong to the current user.

Success response `data`:

```json
{
  "revoked": true
}
```

Current errors:

- `VALIDATION_FAILED`
- `AUTH_LOGIN_REQUIRED`
- `AUTH_FORBIDDEN`
- `AUTH_SESSION_EXPIRED`

## 6. List Login Events

```text
GET /api/v1/auth/login-events
```

Purpose:

- Return a safe login history view for the current user.

Success response `data`:

```json
{
  "items": [
    {
      "id": "1776964564241013",
      "provider": "email",
      "eventType": "email_token_login",
      "success": true,
      "failureReason": null,
      "createdAt": "2026-04-23T17:16:04.241Z"
    }
  ],
  "page": 1,
  "pageSize": 1,
  "total": 1,
  "hasNext": false
}
```

Security notes:

- Ordinary API does not expose `ipAddress`.
- Ordinary API does not expose `ipHash`.
- Ordinary API does not expose `userAgentHash`.

Current errors:

- `AUTH_LOGIN_REQUIRED`

## 7. Current Implementation Notes

- Storage uses `JdbcAuthStore` when `JdbcTemplate` is available.
- Tests use `InMemoryAuthStore` fallback.
- Email delivery is abstracted behind `EmailSender`.
- Default sender is `LoggingEmailSender`.
- Rate limit is currently implemented with database/in-memory counting over a recent time window.
