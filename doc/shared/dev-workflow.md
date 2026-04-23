# Development Workflow

XJTUhub uses documentation-first development.

## General Flow

1. Define or update product constraints when needed.
2. Write backend PRD for a capability.
3. Write or update API and data contracts.
4. Derive frontend page specs.
5. Generate visual prompts and reference images when needed.
6. Implement backend and frontend against the approved docs.
7. For every implemented backend feature, write or update the module API document in `doc/backend/modules/<module>/`.
8. Add tests.
9. Update `TODO.md` and decision docs.

## Frontend And Backend Separation

- Backend docs live under `doc/backend`.
- Module API docs live under `doc/backend/modules/<module>`.
- Frontend docs live under `doc/frontend`.
- Shared terms and process docs live under `doc/shared`.
- Cross-references are allowed.
- Mixed documents that blur business rules and visual implementation should be split.
- API documentation must be split by module. Do not keep growing one global implementation note for all modules.

## Changing Rules

When changing a constraint:

1. Update the owning document.
2. Add a decision log entry if the change affects architecture, workflow, security, or product scope.
3. Update impacted docs.
4. Mention the change in implementation notes or PR description.

## Branch And Commit Guidance

Use focused commits when possible.

Suggested commit prefixes:

- `docs:`
- `feat:`
- `fix:`
- `test:`
- `refactor:`
- `chore:`

## Review Guidance

Reviewers should check:

- Does the change follow the relevant PRD/spec?
- Are frontend and backend responsibilities separated?
- Are permissions enforced on the backend?
- Are provider SDKs isolated behind adapters?
- Are tests included for high-risk behavior?
