# Frontend Implementation Rules

The frontend uses Nuxt 3.

## API Access

- Use a shared API client layer.
- Do not call backend services from random components.
- Do not call Elasticsearch, MinIO, MySQL, Redis, or email services directly.
- Handle backend error codes consistently.

## SSR And CSR

- Use SSR for public content pages when indexing, sharing, or first-read performance matters.
- Use CSR for highly interactive authenticated pages when SSR gives little value.
- Document exceptions in the page spec.

## Permissions

- Backend APIs must provide permission and capability information.
- The frontend may hide unavailable controls, but the backend must still enforce permissions.
- Do not hardcode privileged behavior from role names alone.

## Components

Create reusable components for:

- Content cards.
- Resource review labels.
- Membership badges.
- Anonymous author labels.
- Reaction controls.
- Report controls.
- Empty, loading, error, and no-permission states.

## Visual References

Implementation prompts must include:

- Page spec.
- API contract.
- Selected visual reference images or documented reason for no images.
- Relevant UI rules.
