# Global Foundation PRD

This document is the top-level backend foundation contract for XJTUhub. It defines the decisions that every backend module PRD and implementation plan must inherit.

## 1. Authority

This file is authoritative for global backend design together with:

- `doc/backend/prd/global-data-model.md`
- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/prd/global-auth-organization-permission.md`

If a module PRD conflicts with these files, stop implementation and revise the design first.

AI agents must read `doc/`, not `doc-zh/`, when implementing or reviewing work.

## 2. Product Direction

Phase 1 builds the community content foundation:

1. Community discussion.
2. Resource sharing.
3. Information aggregation.

Campus tools, club management depth, recommendation systems, and multi-client applications are future phases. Phase 1 may reserve data and API extension points, but must not implement complete later-phase systems unless explicitly approved by a module PRD.

## 3. Technical Direction

| Area | Decision |
| --- | --- |
| Backend shape | Spring Boot modular monolith |
| Future extraction option | Rust Axum for pressure-point modules |
| Database source of truth | MySQL |
| Cache/session support | Redis where useful |
| Search | Elasticsearch from Phase 1, index only |
| Object storage | Storage interface, MinIO adapter first |
| Future storage adapters | Aliyun OSS, Tencent COS |
| Frontend consumer | Nuxt 3 |
| API style | REST JSON APIs under `/api/v1` |
| Login state | HttpOnly Cookie session |
| Password login | Forbidden |

## 4. Non-Negotiable Backend Rules

- Use Snowflake `BIGINT` IDs for business tables.
- Serialize all IDs as strings in APIs.
- Use logical foreign keys only; do not create MySQL `FOREIGN KEY` constraints in Phase 1.
- Store database times as UTC `datetime(3)`.
- Use `snake_case` for database names and `camelCase` for API JSON.
- Use the unified API envelope with `requestId` and `durationMs`.
- Keep MySQL as source of truth. Elasticsearch is never authoritative.
- Access MinIO/OSS/COS only through storage adapters.
- Do not add password login, password reset, or password storage.
- Do not expose session tokens to frontend JavaScript.
- Do not expose plaintext IP fields through ordinary user APIs.
- Do not let membership grant moderation, admin, or organization privileges.
- Do not let frontend infer permission, identity, review, or membership state.

## 5. Backend Module Map

The backend is a modular monolith. Modules communicate through public application services or explicitly documented query interfaces.

| Module | Owns | Public Responsibilities |
| --- | --- | --- |
| `auth` | email token login, sessions, auth identities, login events | identify current user, manage sessions |
| `user` | user profile, account status, membership display | provide user summary and display badges |
| `admin` | site admin accounts and admin capabilities | authorize `/admin` operations |
| `organization` | organizations, members, organization roles | authorize organization-scoped actions |
| `content` | boards, tags, contents, content metadata, author display type | publish and read unified content |
| `comment` | comments and replies | manage two-level comment trees |
| `reaction` | content/comment reactions | manage likes, dislikes, favorites |
| `file-storage` | attachments, upload/download, download logs, storage adapters | isolate object storage providers |
| `review` | review tasks, resource trust labels, reports | coordinate moderation workflows |
| `search` | ES index mapping, index tasks, search API | serve filtered search results |
| `notification` | site notifications and email delivery records | notify users through site/email |
| `audit` | append-only sensitive operation logs | record security and admin actions |

## 6. Module Boundary Rules

- A module may read/write only its owned tables directly.
- Cross-module writes must call the owning module application service.
- Cross-module reads must use a documented query service, projection, or read-only DTO provider.
- Provider SDKs belong only in adapter modules.
- Admin workflows may coordinate modules, but must not bypass their domain rules.
- Search indexing consumes authoritative state; it does not own business state.

## 7. Required Implementation Order

The first implementation wave should follow this order:

1. Common foundation: ID generator, time provider, request ID, response envelope, error model.
2. User and auth foundation: users, auth identities, email tokens, sessions, login events.
3. Admin foundation: admin accounts and capability mapping.
4. Organization foundation: organizations, members, member roles.
5. Content foundation: boards, tags, contents, content metadata, author DTO.
6. Comments and reactions.
7. File storage and resource details.
8. Reports and review tasks.
9. Search index tasks and search API.
10. Notifications.
11. Admin APIs.

Do not build frontend pages before the backend PRD and page spec exist.

## 8. Required PRD Sections For Modules

Every module PRD must define:

1. Goal and non-goals.
2. Actors and permissions.
3. User/system actions.
4. State machine.
5. Owned data.
6. APIs and DTOs.
7. Error codes.
8. Audit requirements.
9. Notification behavior.
10. Search indexing behavior when applicable.
11. Tests.
12. Cross-module dependencies.

## 9. Security Baseline

- Campus LAN deployment is an exposure reduction, not a security replacement.
- Backend authorizes every write operation.
- High-sensitive fields require both application authorization and database access separation.
- Admin operations must produce audit logs.
- Rate limits must exist for login, email token creation, posting, commenting, uploading, reporting, dislike actions, and search.

## 10. Handoff Checklist For Implementation Agents

Before coding, an implementation agent must confirm:

- The target module has a module PRD.
- The module PRD references these global PRDs.
- Required tables and APIs do not conflict with the global foundation.
- Security-sensitive fields are not exposed through ordinary DTOs.
- Tests include permission and error cases, not only happy paths.
