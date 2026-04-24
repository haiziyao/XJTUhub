# Agent Guide

This file is the required entry point for AI agents and developers working on XJTUhub.

## Project Purpose

- Build XJTUhub as a campus-oriented discussion, resource sharing, and information aggregation platform.
- Phase 1 prioritizes community discussion, then resource sharing, then information aggregation.
- The system must stay modular enough to grow into tools, clubs, competitions, experience sharing, and multi-client products later.

## Required Reading

Read only the documents relevant to the current task, but always start here.

- Product constraints: `doc/product/product-constraints.md`
- Phase roadmap: `doc/product/phase-roadmap.md`
- Information architecture: `doc/product/information-architecture.md`
- Backend architecture: `doc/backend/backend-architecture.md`
- Frontend workflow: `doc/frontend/frontend-workflow.md`
- Development workflow: `doc/shared/dev-workflow.md`
- Task board: `doc/shared/task-board.md`
- Task completion log: `doc/shared/task-completion-log.md`

## Hard Rules

- AI agents must use `doc/` as the authoritative documentation source. `doc-zh/` is a human-readable Chinese mirror and must not be used as the execution source.
- If `doc/` and `doc-zh/` disagree, follow `doc/` and update `doc-zh/` later.
- Do not implement frontend pages before a backend PRD or page spec exists.
- Keep frontend and backend documents separate. Cross-reference them, but do not merge their responsibilities.
- Backend docs define business capability, data, API, permissions, moderation, storage, and search.
- Frontend docs define page behavior, UI states, visual references, prompt generation, and Nuxt implementation.
- Do not let frontend code decide permissions, review status, membership status, or identity level by itself. These values must come from backend APIs.
- Do not call MinIO, Elasticsearch, email, or campus login providers directly from unrelated business modules. Use adapters and module interfaces.
- Keep MySQL as the source of truth. Elasticsearch is only a search index.
- Anonymous content is only anonymous on the frontend. The backend must preserve traceability.
- Membership is a presentation and future benefits layer. It does not grant moderation or administration permissions.

## Current Technology Direction

- Frontend: Nuxt 3.
- Backend: Spring Boot modular monolith.
- Database: MySQL.
- Cache/session support: Redis.
- Object storage: MinIO through a storage interface, with future Aliyun OSS and Tencent COS adapters.
- Search: Elasticsearch from Phase 1.
- Deployment: local development, campus LAN, and future public internet modes.

## Development Notes

- Update `doc/shared/task-board.md` and `doc/shared/task-completion-log.md` when starting or finishing meaningful project work.
- Update the relevant document before changing a boundary, workflow, or rule.
- Use `doc/shared/decision-log.md` for durable decisions and tradeoffs.
- Keep implementation details out of top-level documents unless they are needed for navigation.
