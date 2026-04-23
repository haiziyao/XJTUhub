# Search

XJTUhub uses Elasticsearch from Phase 1.

## Source Of Truth

- MySQL is the source of truth.
- Elasticsearch is a search index.
- Never use Elasticsearch as the authoritative store for content, permissions, review state, or user identity.

## Search Module

The backend `search` module owns:

- Index mapping.
- Index write tasks.
- Reindexing.
- Search API.
- Search result DTOs.
- Search diagnostics.

Other modules should publish search indexing events or call search application services.

## Indexing

Content indexing must be asynchronous and retryable.

Required behavior:

- Published or updated content updates the index.
- Hidden, deleted, rejected, or permission-changed content updates or removes index visibility.
- Failed indexing jobs can be retried.
- Full reindexing can rebuild from MySQL.

## Index Versioning

Use versioned index names such as:

```text
xjtuhub_contents_v1
```

Mapping changes that require reindexing should create a new versioned index.

## Initial Content Index Fields

Suggested fields:

- content id.
- type.
- title.
- body excerpt.
- tags.
- board id.
- board name.
- author id.
- author display name.
- anonymous display state.
- visibility.
- status.
- review status.
- published time.
- updated time.
- popularity score.

## Frontend Access

The frontend must call backend search APIs. It must not call Elasticsearch directly.
