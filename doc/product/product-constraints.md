# Product Constraints

This document defines product boundaries for XJTUhub. Backend and frontend documents may reference these rules, but must not silently rewrite them.

## Product Positioning

XJTUhub is a campus-oriented platform for discussion, resource sharing, and information aggregation.

Phase 1 must focus on one core loop:

1. Users discover boards and content.
2. Users publish or interact with discussion content.
3. Users attach or find resources when relevant.
4. Users follow boards, receive notifications, and return.

## Priority Order

The product priority is:

1. Community discussion.
2. Resource sharing.
3. Information aggregation.
4. Campus tools, clubs, competitions, and advanced services.

Do not let lower-priority modules delay the Phase 1 community/content foundation.

## Identity

- Email registration is allowed and does not require an XJTU email domain.
- Campus official APP scan login is the preferred high-trust identity source.
- Identity trust must be modeled as a backend value, not inferred by the frontend.
- Future campus LAN deployment may reduce exposure, but must not replace authentication, permissions, moderation, audit logs, and rate limits.

## Membership

Membership is separate from role and identity trust.

Phase 1 membership behavior:

- Premium members have a red nickname.
- Premium members have a visible premium badge.
- Membership does not grant moderation, administration, pinning, deletion, review, or ranking privileges.
- Hidden, banned, deleted, or revoked accounts must not display active membership benefits.

## Anonymous Content

- Anonymous publishing means frontend anonymity only.
- The backend must preserve the real author for audit, reporting, moderation, and abuse prevention.
- Anonymous content must support dislike interactions.
- Dislikes are moderation and quality signals, not direct deletion votes.

## Moderation Level

XJTUhub uses medium moderation:

- Ordinary posts and comments may be visible after publishing.
- Resources, pinned content, announcements, sensitive boards, and official organization content require explicit review state.
- Reports and moderation actions must be traceable.

## Resource Trust Labels

Uploaded resources may be downloadable immediately.

- Unreviewed resources must show a red or warning label.
- Verified resources must show a green or trusted label.
- Labels must include text or icon support, not color alone.
- Rejected or disputed resources are controlled by backend policy.

## Non-Goals For Phase 1

- Full campus tool suite.
- Full club management system.
- Complex analytics reports.
- Personalized recommendation engine.
- Multi-client Flutter or Tauri applications.
