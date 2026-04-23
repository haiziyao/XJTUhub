# UI Design Rules

XJTUhub uses Nuxt 3 for the frontend.

## Rendering Strategy

- Public content pages should prefer SSR.
- Logged-in interactive pages may use CSR.
- Admin pages may use CSR unless public indexing is required.
- Do not make all pages pure CSR without documenting why public indexing and sharing previews are irrelevant.

## SEO And Sharing

Public content pages should support:

- meaningful title.
- description.
- canonical URL where applicable.
- share preview metadata.
- readable server-rendered content where possible.

## Required Visual States

The UI must clearly represent:

- Anonymous content.
- Premium membership red name and badge.
- Unreviewed resource warning or red label.
- Verified resource trusted or green label.
- Rejected or disputed resource state.
- User role and permission limitations where relevant.
- Loading, empty, error, and no-permission states.

## Color And Accessibility

- Red and green labels must include text or icon support.
- Do not rely on color alone.
- Interactive controls must have clear focus and disabled states.
- Mobile layouts must not hide moderation or resource trust labels.

## Component Rules

- Shared API state display should be componentized.
- Permission-sensitive controls should render from backend-provided capability flags where possible.
- Do not hardcode role checks only in the frontend.
- Use a unified content card for search, board lists, and profile lists unless a page spec requires otherwise.
