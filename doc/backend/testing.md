# Backend Testing

Backend tests must focus on business boundaries and high-risk integrations.

## Required Test Levels

- Unit tests for domain rules.
- Module tests for application services.
- API tests for externally visible behavior.
- Integration tests for storage adapters and Elasticsearch indexing where practical.

## Required Coverage Areas

High-risk areas require tests:

- Permission checks.
- Anonymous traceability.
- Resource review labels and download policy.
- Membership display state.
- Board posting policies.
- Report and dislike behavior.
- Object storage adapter behavior.
- Search indexing and reindexing.
- Audit log creation for sensitive actions.

## Test Data

Tests should include:

- Email user.
- Campus verified user.
- Premium member.
- Moderator.
- Admin.
- Anonymous content.
- Unreviewed resource.
- Verified resource.
- Disputed resource.

## Regression Rule

When fixing a bug, add a test that fails without the fix unless the bug is purely configuration or documentation.
