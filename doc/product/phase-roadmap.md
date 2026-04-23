# Phase Roadmap

This roadmap describes implementation phases. It is not a promise that every later feature must be built.

## Phase 1: Community Content Foundation

Goal: build a usable community platform with content, resources, search, basic moderation, and notifications.

Scope:

- Nuxt 3 frontend.
- Spring Boot modular monolith backend.
- MySQL as source of truth.
- Redis for cache/session/rate-limit support.
- MinIO through a storage abstraction.
- Elasticsearch for content search from the start.
- Email registration and planned campus official APP scan login.
- Unified content model for posts, resources, activities, experiences, blogs, and tools.
- Medium moderation.
- Site notifications and important email notifications.
- Basic admin console for users, content, reports, resources, boards, tags, announcements, and audit logs.

## Phase 2: Module Depth

Goal: deepen high-value content modules after the core loop works.

Possible scope:

- Stronger resource library with course, teacher, semester, version, and rating fields.
- Better activity and competition aggregation.
- More detailed experience sharing templates.
- Improved notification subscriptions and email preferences.
- Richer moderation workflow.
- Better Elasticsearch relevance, synonyms, and Chinese search tuning.
- Frontend visual workflow maturity with generated references per page state.

## Phase 3: Ecosystem Expansion

Goal: expand beyond the core community platform.

Possible scope:

- Campus tools integration.
- Club and organization management.
- Recommendation and semantic search.
- More advanced analytics.
- Mobile or desktop clients.
- Selected backend module extraction to Rust Axum when hardware pressure or performance requires it.

## Defer Rules

- Do not implement Phase 2 or Phase 3 features unless they directly support Phase 1 stability.
- Preserve extension points, but do not build full systems prematurely.
- A future module must receive its own PRD before implementation.
