# Backend Architecture

The Phase 1 backend is a Spring Boot modular monolith.

## Architecture Goals

- Keep deployment simple for Phase 1.
- Keep module boundaries strict enough for future extraction.
- Avoid framework-coupled business rules where practical.
- Make storage, search, email, and campus login providers replaceable.

## Modules

Recommended backend modules:

- `auth`: email login, campus scan login, sessions, identity trust, authorization helpers.
- `user`: user profile, account status, membership display state, settings.
- `content`: unified content model, boards, tags, publishing.
- `comment`: comments, replies, comment moderation, comment reactions.
- `review`: resource verification, pinned content review, announcement review, moderation tasks.
- `file-storage`: upload, download, object storage adapters, attachment metadata.
- `notification`: site notifications and important email notifications.
- `search`: Elasticsearch indexing and search API.
- `admin`: management workflows that coordinate other modules.
- `audit`: administrator actions, security logs, and sensitive operation logs.

## Module Boundary Rules

- Split by business capability, not by technical layer alone.
- A module may expose application services or interfaces for other modules.
- Do not access another module's internal repositories, entities, or implementation classes directly.
- Each module owns its tables. Cross-module reads require a documented query service, projection, or read model.
- Cross-module writes must go through the owning module's application service.

## Framework Isolation

- Domain rules should be ordinary Java code where practical.
- HTTP DTOs, ORM entities, and framework annotations must not become the only representation of business rules.
- Provider SDKs must be isolated behind adapters.

## Future Axum Migration

Rust Axum is a future option for pressure points, not a Phase 1 dependency.

Good extraction candidates:

- `file-storage`
- `search`
- `notification`

Bad extraction candidates at first:

- Shared content rules before the domain is stable.
- Auth before campus login and permission rules are stable.
