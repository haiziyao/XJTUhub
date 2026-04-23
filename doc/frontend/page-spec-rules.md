# Page Spec Rules

Every non-trivial frontend page must have a page spec before implementation.

## Required Sections

Each page spec must include:

- Page goal.
- Source backend PRD.
- User roles and auth states.
- API dependencies.
- Main layout.
- User actions.
- Loading state.
- Empty state.
- Error state.
- No-permission state.
- Desktop behavior.
- Mobile behavior.
- Accessibility notes.
- Visual reference requirements.

## Backend Relationship

- Page specs may reference backend PRDs and API contracts.
- Page specs may not change business rules.
- If a frontend need reveals a backend gap, update or create the backend PRD first.

## State Coverage

For content pages, cover:

- Normal published content.
- Anonymous content.
- Premium author.
- Unreviewed resource.
- Verified resource.
- Deleted or hidden content.
- No permission.

For admin pages, cover:

- List state.
- Filtering state.
- Detail state.
- Confirmation state.
- Failure state.
- Audit visibility.
