# Global API Contracts PRD

This document defines the API contract that every backend endpoint must follow unless a module PRD explicitly documents an exception.

## 1. Goals

- Provide a stable REST contract for Nuxt 3 and future clients.
- Make backend performance visible through `durationMs`.
- Keep permission and identity decisions backend-owned.
- Separate ordinary user APIs from admin APIs.
- Keep file, search, and provider integrations behind backend APIs.

## 2. Namespace

All JSON APIs use:

```text
/api/v1
```

Initial namespaces:

```text
/api/v1/auth
/api/v1/users
/api/v1/organizations
/api/v1/boards
/api/v1/tags
/api/v1/contents
/api/v1/comments
/api/v1/reactions
/api/v1/files
/api/v1/search
/api/v1/notifications
/api/v1/admin
```

Rules:

- Admin APIs must be under `/api/v1/admin`.
- Ordinary APIs must not reuse admin DTOs.
- Module-specific PRDs may add subroutes but must keep this namespace style.

## 3. Response Envelope

Every normal JSON response uses the same top-level envelope.

Success:

```json
{
  "data": {},
  "error": null,
  "requestId": "req_01HY0000000000000000000000",
  "durationMs": 18
}
```

Failure:

```json
{
  "data": null,
  "error": {
    "code": "CONTENT_NOT_FOUND",
    "message": "Content not found.",
    "details": {}
  },
  "requestId": "req_01HY0000000000000000000000",
  "durationMs": 12
}
```

`durationMs` rules:

- Unit is milliseconds.
- Value is measured on the backend.
- Value covers request entry to response generation.
- Value is user-visible.
- Ordinary responses must not expose internal SQL, ES, Redis, MinIO, OSS, COS, or email-provider segment timings.

`requestId` rules:

- Must be generated for every request if not provided by an upstream gateway.
- Must be returned to the client.
- Must be included in application logs and audit logs where relevant.

## 4. Non-Envelope Endpoints

File downloads and streaming endpoints may skip the JSON envelope.

Required headers:

```text
X-Request-Id: req_...
X-Duration-Ms: 18
```

If a file/streaming request fails before the body starts, return the standard JSON error envelope when practical.

## 5. ID Contract

Database:

```text
BIGINT Snowflake ID
```

API:

```json
{
  "id": "738912739182739128",
  "authorId": "738912739182739129"
}
```

Rules:

- Every API ID is a string.
- Every path ID is accepted as a string.
- Frontend must not convert IDs to JavaScript numbers.
- Backend validates numeric string format before parsing.

## 6. Pagination

Use offset pagination for stable admin lists and ordinary search/browse lists.

Request:

```text
GET /api/v1/contents?page=1&pageSize=20
```

Response data:

```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "total": 128,
  "hasNext": true
}
```

Use cursor pagination for feeds, timelines, and comment streams.

Response data:

```json
{
  "items": [],
  "nextCursor": "opaque_cursor",
  "hasNext": true
}
```

Rules:

- `page` starts at `1`.
- Default `pageSize` is `20`.
- Maximum `pageSize` is `100` unless the endpoint PRD lowers it.
- Cursors are opaque and must not be parsed by clients.
- Invalid pagination returns `VALIDATION_FAILED`.

## 7. Filtering And Sorting

Common filters:

```text
type
status
reviewStatus
visibility
boardId
tag
authorUserId
organizationId
createdFrom
createdTo
publishedFrom
publishedTo
```

Common sort fields:

```text
createdAt
publishedAt
updatedAt
viewCount
popularityScore
```

Rules:

- Each endpoint PRD must allowlist filters and sort fields.
- Unknown filters return `VALIDATION_FAILED`.
- `order` supports `asc` and `desc`.
- Default sort must be documented per endpoint.

## 8. Error Codes

Error codes are stable `UPPER_SNAKE_CASE` strings.

Global codes:

```text
VALIDATION_FAILED
RATE_LIMITED
INTERNAL_ERROR
SERVICE_UNAVAILABLE
```

Auth/user codes:

```text
AUTH_LOGIN_REQUIRED
AUTH_FORBIDDEN
AUTH_SESSION_EXPIRED
AUTH_EMAIL_TOKEN_INVALID
AUTH_EMAIL_TOKEN_EXPIRED
AUTH_EMAIL_TOKEN_CONSUMED
USER_NOT_FOUND
USER_BANNED
```

Admin/organization codes:

```text
ADMIN_ACCOUNT_REQUIRED
ADMIN_ACCOUNT_REVOKED
ADMIN_CAPABILITY_REQUIRED
ORGANIZATION_NOT_FOUND
ORGANIZATION_PERMISSION_DENIED
ORGANIZATION_MEMBER_NOT_FOUND
```

Content/comment/file/review/search codes:

```text
BOARD_NOT_FOUND
BOARD_CLOSED
TAG_NOT_FOUND
CONTENT_NOT_FOUND
CONTENT_NOT_EDITABLE
CONTENT_VISIBILITY_DENIED
COMMENT_NOT_FOUND
COMMENT_NOT_EDITABLE
REACTION_NOT_ALLOWED
FILE_UPLOAD_NOT_ALLOWED
FILE_NOT_FOUND
FILE_DOWNLOAD_BLOCKED
REVIEW_REQUIRED
REVIEW_TASK_NOT_FOUND
REPORT_NOT_FOUND
SEARCH_QUERY_INVALID
```

Rules:

- `code` is for programs.
- `message` is safe for display and later i18n.
- `details` contains only safe information.
- Do not expose SQL errors, stack traces, provider internals, object keys for unauthorized users, or plaintext security-sensitive values.

## 9. Validation Details

Field validation errors use:

```json
{
  "code": "VALIDATION_FAILED",
  "message": "Validation failed.",
  "details": {
    "fields": {
      "title": "Title is required.",
      "pageSize": "pageSize must be between 1 and 100."
    }
  }
}
```

Rules:

- Use API JSON field names in validation details.
- Do not expose database column names unless they are identical and safe.

## 10. Capabilities DTO

Actionable resources should include backend-computed `capabilities`.

Example:

```json
{
  "id": "738912739182739128",
  "title": "Advanced Math Review Notes",
  "capabilities": {
    "canEdit": true,
    "canDelete": false,
    "canComment": true,
    "canReport": true,
    "canReview": false,
    "canPin": false,
    "canDownload": true
  }
}
```

Rules:

- `capabilities` is for UI rendering.
- Backend must still authorize every write operation.
- Frontend must not infer permissions from role names, email domain, login provider, membership, or identity badge.
- Capability names are stable API surface.

## 11. Badge DTO

Badge shape:

```json
{
  "type": "identity",
  "code": "campus_verified",
  "label": "Campus Verified",
  "tone": "trusted"
}
```

Initial `type` values:

```text
identity
membership
organization
moderation
```

Initial `tone` values:

```text
neutral
trusted
warning
danger
premium
official
```

Rules:

- Badge labels are returned by backend for now.
- Future i18n may replace labels with localization keys.
- Frontend renders badges; it does not decide identity truth.

## 12. Author DTO

All content-like and comment-like DTOs use a unified author shape.

User author:

```json
{
  "displayType": "user",
  "userId": "1001",
  "nickname": "Xuan Ziling",
  "avatarUrl": "https://example.com/avatar.png",
  "nameColor": "red",
  "displayBadges": [
    {
      "type": "identity",
      "code": "email_verified",
      "label": "Email Verified",
      "tone": "neutral"
    },
    {
      "type": "membership",
      "code": "premium",
      "label": "Premium",
      "tone": "premium"
    }
  ]
}
```

Organization author:

```json
{
  "displayType": "organization",
  "organizationId": "2001",
  "organizationName": "Example Club",
  "avatarUrl": "https://example.com/org.png",
  "displayBadges": [
    {
      "type": "organization",
      "code": "club",
      "label": "Club",
      "tone": "official"
    }
  ]
}
```

Anonymous author:

```json
{
  "displayType": "anonymous",
  "anonymousName": "Anonymous User",
  "displayBadges": []
}
```

Rules:

- Ordinary APIs never reveal anonymous real author.
- Admin APIs may include `realAuthorUserId` only with permission.
- Organization author DTO must not expose the operator in ordinary APIs.

## 13. Content Card DTO

Search results, board lists, user profile lists, and related content should use a common card shape:

```json
{
  "id": "738912739182739128",
  "type": "resource",
  "title": "Advanced Math Review Notes",
  "excerpt": "A concise review pack...",
  "board": {
    "id": "11",
    "name": "Resources",
    "slug": "resources"
  },
  "tags": [
    {
      "id": "21",
      "name": "Math",
      "slug": "math"
    }
  ],
  "author": {},
  "status": "published",
  "visibility": "public",
  "reviewStatus": "unreviewed",
  "publishedAt": "2026-04-23T13:20:00.123Z",
  "stats": {
    "views": 120,
    "comments": 4,
    "likes": 12,
    "dislikes": 1,
    "favorites": 8
  },
  "capabilities": {}
}
```

## 14. File API Contract

Suggested routes:

```text
POST   /api/v1/files/uploads
POST   /api/v1/files/uploads/{uploadId}/complete
GET    /api/v1/files/{attachmentId}
GET    /api/v1/files/{attachmentId}/download
DELETE /api/v1/files/{attachmentId}
```

Rules:

- Frontend never calls MinIO, Aliyun OSS, or Tencent COS directly.
- Backend decides upload permission.
- Backend decides download permission.
- Download writes `file_download_logs`.
- Unreviewed files are downloadable unless backend policy blocks them, but DTOs must show unreviewed state.
- Download endpoint returns `X-Request-Id` and `X-Duration-Ms`.

## 15. Search API Contract

Suggested route:

```text
GET /api/v1/search/contents
```

Rules:

- Frontend never calls Elasticsearch directly.
- Search API filters by current user's visibility permissions.
- Search result cards reuse Author DTO, Badge DTO, and Content Card DTO rules.
- Elasticsearch stores index data only; MySQL remains authoritative.

## 16. Admin API Contract

Admin routes:

```text
/api/v1/admin/users
/api/v1/admin/contents
/api/v1/admin/comments
/api/v1/admin/files
/api/v1/admin/reports
/api/v1/admin/reviews
/api/v1/admin/boards
/api/v1/admin/tags
/api/v1/admin/organizations
/api/v1/admin/audit-logs
```

Rules:

- Every admin route requires an active `admin_accounts` record.
- Every sensitive action requires a capability check.
- Admin DTOs may include moderation fields not present in ordinary APIs.
- Admin DTOs must not expose plaintext IP by default.
- Security-audit endpoints need a separate high-trust capability.

## 17. Reserved Campus Login APIs

Campus APP scan-login routes may be reserved but are not Phase 1 implementation tasks:

```text
POST /api/v1/auth/campus-scan/sessions
GET  /api/v1/auth/campus-scan/sessions/{sceneId}
POST /api/v1/auth/campus-scan/sessions/{sceneId}/confirm
```

Rules:

- Mark these routes as reserved until the real campus protocol is known.
- Do not implement fake protocol behavior as production behavior.
