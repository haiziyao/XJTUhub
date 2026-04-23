# User Module API

This document describes the currently implemented `user` module APIs.

Base path:

```text
/api/v1/users
```

Global response envelope, error handling, and DTO constraints inherit:

- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/prd/global-auth-organization-permission.md`

## 1. Get Current User

```text
GET /api/v1/users/me
```

Purpose:

- Return the current authenticated user's profile summary for ordinary client use.

Success response `data`:

```json
{
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
}
```

Behavior notes:

- `nameColor` becomes `red` when the user has active premium membership.
- `displayBadges` is backend-computed.

Current errors:

- `AUTH_LOGIN_REQUIRED`
- `USER_NOT_FOUND`

## 2. Update Current User

```text
PATCH /api/v1/users/me
```

Purpose:

- Update the current user's editable profile fields.

Request:

```json
{
  "nickname": "Profile User",
  "bio": "Updated profile bio",
  "avatarUrl": "https://example.com/avatar.png"
}
```

Current validation:

- `nickname` is required.
- `nickname` max length is `64`.
- `bio` max length is `512`.
- `avatarUrl` max length is `512`.

Success response:

- Returns the same DTO shape as `GET /api/v1/users/me`.

Current errors:

- `VALIDATION_FAILED`
- `AUTH_LOGIN_REQUIRED`
- `USER_NOT_FOUND`

## 3. Current Implementation Notes

- Profile updates currently write directly to `users.nickname`, `users.bio`, and `users.avatar_url`.
- No separate avatar upload workflow exists yet; `avatarUrl` is currently a raw URL field.
- Further user profile capabilities should be documented in this file as they are implemented.
