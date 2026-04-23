# Backend PRD Rules

Every meaningful backend capability must have a PRD before implementation.

## Purpose

Backend PRDs define business behavior, data, permissions, state transitions, APIs, and operational constraints. Frontend page specs are derived from backend PRDs.

## Required Sections

Each backend PRD must include:

- Goal.
- User roles and identity requirements.
- User actions.
- Permission rules.
- Data model impact.
- State machine or status values.
- API contracts.
- Error cases.
- Audit and moderation requirements.
- Notification behavior.
- Search indexing behavior when applicable.
- Testing expectations.

## Frontend Relationship

- Backend PRDs must not prescribe exact visual design.
- Backend PRDs may define what data and states the frontend must expose.
- Frontend page specs must reference the relevant backend PRD.
- Frontend specs may not change backend business rules.

## Quality Bar

Do not accept a PRD that leaves these undefined:

- Who can perform the action.
- What happens after success.
- What happens after failure.
- Which data is authoritative.
- Which states are visible to users.
- Which actions require audit logs.
