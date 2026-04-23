# API Contracts

Backend APIs are the only supported integration point for the frontend.

## API Ownership

- The frontend must not connect directly to MySQL, Redis, Elasticsearch, MinIO, Aliyun OSS, Tencent COS, or email providers.
- API responses must expose permission, review, identity, and membership states explicitly.
- The frontend must not derive privileged states from local assumptions.

## Response Shape

Use a consistent response envelope unless a framework-specific reason is documented.

Recommended shape:

```json
{
  "data": {},
  "error": null,
  "requestId": "string"
}
```

Error shape:

```json
{
  "data": null,
  "error": {
    "code": "CONTENT_NOT_FOUND",
    "message": "Content not found",
    "details": {}
  },
  "requestId": "string"
}
```

## Pagination

List APIs must define pagination explicitly.

Recommended fields:

- `items`
- `page`
- `pageSize`
- `total`
- `hasNext`

Cursor pagination may be used for feeds and timelines when offset pagination is not appropriate.

## Versioning

- Public API paths should be versioned, for example `/api/v1`.
- Breaking changes require a documented migration.
- Frontend page specs must reference the API version they depend on.

## Authentication And Authorization

- APIs must validate permissions on the backend.
- Authentication failure and authorization failure must use distinct error codes.
- Sensitive actions require audit logs.

## Content Cards

Search, board lists, profile lists, and related-content lists should return a unified content card DTO where possible:

- id.
- type.
- title.
- excerpt.
- board.
- tags.
- author display.
- anonymous display state.
- membership display state.
- review label.
- reaction summary.
- published time.
