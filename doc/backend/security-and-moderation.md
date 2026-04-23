# Security And Moderation

Security must not rely on campus LAN deployment alone.

## Login Methods

Supported direction:

- Email registration and login.
- Campus official APP scan login as high-trust identity.

Email does not need to be an XJTU domain.

## Permission Model

Permission decisions combine:

- Role.
- Auth level.
- Board policy.
- Content state.

Suggested board policies:

- `open_publish`
- `review_required`
- `verified_only`
- `admin_only`

## Moderation Model

XJTUhub uses medium moderation:

- Ordinary posts and comments can be visible after publishing.
- Resources can be downloadable immediately but must show review state.
- Official, pinned, announcement, sensitive, and admin-controlled content may require review.

## Anonymous Content

- Anonymous content hides author identity in normal frontend views.
- Backend must preserve author identity.
- Admin and moderation workflows may reveal traceability where authorized.
- Anonymous content supports dislike reactions.

## Reports And Dislikes

- Reports create moderation records.
- Dislikes are quality and risk signals.
- Dislikes must not directly delete content.
- Repeated dislikes can trigger folding, ranking changes, or review tasks according to backend policy.

## Audit Logs

Audit logs are required for:

- Admin login.
- User ban or unban.
- Content hide or restore.
- Resource verification changes.
- Report resolution.
- Permission changes.
- Membership grant, revoke, or expiry correction.
- Storage deletion.

## Rate Limits

Rate limits should be considered for:

- Login attempts.
- Email verification.
- Posting.
- Commenting.
- Uploading.
- Reporting.
- Dislike actions.
- Search requests.
