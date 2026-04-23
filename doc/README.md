# XJTUhub Documentation Map

XJTUhub uses documentation-first development. This map explains where each kind of decision belongs.

## Authority

`doc/` is the authoritative documentation source for AI agents, implementation work, and reviews.

`doc-zh/` is a Chinese mirror for human reading only. If the two directories disagree, follow `doc/` and update `doc-zh/` later.

## Reading Order

1. Start with `../Agent.md` for agent and contributor rules.
2. Read product constraints before changing scope.
3. Read backend documents before defining APIs, data, permissions, storage, search, or moderation.
4. Read frontend documents before designing pages, generating visual prompts, or writing Nuxt code.
5. Record durable decisions in `shared/decision-log.md`.

## Product Documents

| Document | Purpose |
| --- | --- |
| `product/product-constraints.md` | Product boundaries, identity, membership, anonymous content, moderation, and resource trust labels. |
| `product/phase-roadmap.md` | Phase 1 MVP and later expansion roadmap. |
| `product/information-architecture.md` | Boards, tags, navigation, and unified content types. |

## Backend Documents

| Document | Purpose |
| --- | --- |
| `backend/backend-architecture.md` | Spring Boot modular monolith boundaries and future Axum extraction principles. |
| `backend/backend-prd-rules.md` | Required structure for backend PRDs. |
| `backend/data-model-rules.md` | Logical entities, unified content model, identity, membership, and review states. |
| `backend/api-contracts.md` | API response, errors, pagination, versioning, and frontend integration rules. |
| `backend/storage-and-files.md` | Object storage abstraction, MinIO adapter, future OSS/COS adapters, and attachment policy. |
| `backend/search.md` | Elasticsearch source-of-truth boundary, indexing, and search API rules. |
| `backend/security-and-moderation.md` | Login, permissions, moderation, anonymity, reports, dislikes, audit, and rate limits. |
| `backend/testing.md` | Backend testing expectations and high-risk coverage areas. |

### Backend PRDs

| Document | Purpose |
| --- | --- |
| `backend/prd/global-foundation.md` | Global data/API foundation, module boundaries, and implementation dependency order. |
| `backend/prd/global-data-model.md` | Logical model and suggested MySQL physical table design. |
| `backend/prd/global-api-contracts.md` | REST API envelope, duration, pagination, errors, DTOs, file APIs, search APIs, and admin APIs. |
| `backend/prd/global-auth-organization-permission.md` | Email login, reserved campus login, sessions, admin accounts, organizations, roles, authoring, and visibility. |

## Frontend Documents

| Document | Purpose |
| --- | --- |
| `frontend/frontend-workflow.md` | Backend PRD -> page spec -> prompt -> gpt-image-2 references -> Nuxt implementation workflow. |
| `frontend/ui-design-rules.md` | SSR/CSR policy, SEO, required UI states, and accessibility constraints. |
| `frontend/page-spec-rules.md` | Required structure for frontend page specs. |
| `frontend/image-prompt-rules.md` | Rules for generating and using visual reference prompts. |
| `frontend/implementation-rules.md` | Nuxt implementation, API client, permissions, components, and visual-reference rules. |

## Shared Documents

| Document | Purpose |
| --- | --- |
| `shared/glossary.md` | Shared terms used by product, backend, and frontend docs. |
| `shared/decision-log.md` | Durable decisions and reasons. |
| `shared/dev-workflow.md` | Documentation-first workflow, separation rules, and review guidance. |

## Ownership Rule

When a rule crosses frontend and backend concerns, place the source of truth in the owning area and cross-reference it elsewhere. Do not duplicate full rules in multiple places unless the duplication is intentional and kept in sync.
