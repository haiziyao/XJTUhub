# XJTUhub MySQL Bootstrap SQL

These SQL files initialize the Phase 1 MySQL schema.

## Execution Order

Run files in filename order:

```text
000_database.sql
001_user_auth.sql
002_admin_organization.sql
003_content.sql
004_comment_reaction.sql
005_file_storage.sql
006_moderation.sql
007_notification.sql
008_audit_search_reserved.sql
009_views.sql
010_seed_initial_taxonomy.sql
011_permissions_template.sql
```

## Design Rules

- MySQL is the source of truth.
- IDs are backend-generated Snowflake `BIGINT`.
- API IDs must still be serialized as strings.
- No MySQL `FOREIGN KEY` constraints are created.
- Reference columns are logical foreign keys and are indexed.
- Timestamps use `datetime(3)` and should be treated as UTC.
- Plaintext IP fields are high-sensitive and must not be returned by ordinary APIs.

## Environment Notes

Redis, MinIO, and Elasticsearch do not use these SQL files.

- Redis is used for cache/session/rate-limit support where needed.
- MinIO is accessed through the storage adapter; object metadata is stored in MySQL `attachments`.
- Elasticsearch can be deployed later; MySQL `search_index_tasks` is ready for retryable indexing jobs.

## Permission Template

`011_permissions_template.sql` is intentionally a template. Replace usernames, hosts, and passwords before running it.

It demonstrates the intended split:

- normal application account can read public views without plaintext IP
- security audit account can read security views with plaintext IP
