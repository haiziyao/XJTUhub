# Decision Log

Record durable product and engineering decisions here.

## 2026-04-23: Phase 1 Priority

Decision: Phase 1 prioritizes community discussion, then resource sharing, then information aggregation.

Reason: This creates the shortest useful loop while leaving room for resources, activities, tools, clubs, and experience sharing.

## 2026-04-23: Frontend Framework

Decision: Use Nuxt 3.

Reason: XJTUhub may accumulate public content, experience articles, resource indexes, and shareable pages. Nuxt 3 supports Vue-based SSR and CSR mixing.

## 2026-04-23: Backend Architecture

Decision: Use a Spring Boot modular monolith for Phase 1.

Reason: A modular monolith keeps deployment simple while preserving boundaries for future extraction.

## 2026-04-23: Future Backend Extraction

Decision: Rust Axum is a future option for pressure points.

Reason: Some modules may need better performance or lower resource usage later, but premature service splitting would slow Phase 1.

## 2026-04-23: Search

Decision: Use Elasticsearch from Phase 1.

Reason: Search is central to content, resources, and information aggregation. MySQL remains the source of truth.

## 2026-04-23: Object Storage

Decision: Use an object storage abstraction with MinIO as Phase 1 adapter.

Reason: The project already has MinIO, but future Aliyun OSS and Tencent COS migration should not affect business modules.

## 2026-04-23: Frontend Visual Workflow

Decision: Frontend implementation follows backend PRD -> page spec -> image prompts -> gpt-image-2 references -> code implementation.

Reason: This keeps frontend design grounded in backend behavior while allowing high-quality visual exploration before code.
