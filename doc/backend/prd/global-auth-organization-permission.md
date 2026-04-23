# Global Auth, Organization, And Permission PRD

This document defines the global identity, login, session, admin, organization, and permission model.

## 1. Design Goals

- Avoid password storage and password reset.
- Support email verification-code or magic-link login.
- Reserve campus official APP scan login without implementing it in Phase 1.
- Separate user identity from site admin authority.
- Separate site admin authority from organization roles.
- Support users holding multiple positions across multiple organizations.
- Let backend compute identity badges, membership badges, author display, and capabilities.

## 2. Identity Layers

Identity and authority are separate layers:

| Layer | Tables | Purpose |
| --- | --- | --- |
| User account | `users` | Stable account/profile/status |
| Login identities | `user_auth_identities` | Email and future campus APP bindings |
| Membership | `user_memberships` | Red name and premium badge, future benefits |
| Site admin | `admin_accounts` | Site-level moderation/admin access |
| Organization | `organizations` | Club/department/project identity |
| Organization membership | `organization_members`, `organization_member_roles` | Organization-scoped positions |

Rules:

- `users` does not contain site admin role.
- `users` does not contain organization roles.
- `admin_accounts` must bind to `users.id`.
- `organization_member_roles` apply only inside one organization.
- Membership never grants admin, moderation, or organization authority.

## 3. Email Login

Phase 1 login method:

```text
email verification code / magic link
```

Forbidden:

```text
password login
password reset
password hash storage
```

Email token rules:

- Store only token hash.
- Token has `expires_at`.
- Token is single-use.
- Successful use sets `consumed_at`.
- Token creation is rate-limited.
- Token verification is rate-limited.
- Successful email login marks the email identity as `verified`.
- Email domain does not need to be XJTU.

Email login result:

- Creates or finds `users`.
- Creates or updates `user_auth_identities(provider=email)`.
- Creates a `sessions` row.
- Writes `user_login_events`.
- Sets HttpOnly Cookie.

## 4. Campus APP Scan Login

Campus official APP scan login is reserved.

Phase 1 does not implement the real protocol.

Reserved concepts:

- `provider = campus_app`
- `auth_level = campus_app_verified`
- `campus_app_login_sessions`
- reserved API routes under `/api/v1/auth/campus-scan`

Rules:

- Do not block Phase 1 on unknown campus protocol details.
- Do not implement fake scan-login behavior as if it were production.
- Admins may manually mark trusted users as `campus_app_verified` until integration exists.
- Future real integration must update this PRD.

## 5. Auth Levels

Initial auth levels:

```text
email_user
campus_app_verified
official_org
```

Rules:

- `email_user` means the user has a verified email identity.
- `campus_app_verified` means the user has a verified campus identity or temporary admin-approved campus mark.
- `official_org` is reserved for official organization identity cases.
- Backend uses `auth_level` for permission decisions.
- Frontend uses backend-provided badges and capabilities, not raw inference.

## 6. Sessions

Use HttpOnly Cookie sessions.

Session rules:

- Cookie stores only an opaque token.
- Database stores `session_token_hash`.
- Frontend JavaScript cannot read the token.
- Nuxt SSR and browser CSR both use the Cookie.
- Logout revokes the session and clears the Cookie.
- Banning a user revokes active sessions.
- Admin access reuses the user session plus `admin_accounts` authorization.

Session statuses:

```text
active
revoked
expired
```

Session management APIs:

```text
GET    /api/v1/auth/sessions
DELETE /api/v1/auth/sessions/{sessionId}
DELETE /api/v1/auth/sessions/current
```

## 7. Login Events

`user_login_events` is append-only.

Record:

- Known `user_id`, if available.
- Provider.
- Event type.
- Success flag.
- Failure reason.
- Plaintext IP.
- IP hash.
- User agent hash.
- Created time.

Rules:

- Plaintext IP is high-sensitive.
- Ordinary APIs never expose plaintext IP.
- Use logs for security investigation and rate-limit tuning.

## 8. User Display Badges

Backend computes `displayBadges`.

Identity badge codes:

```text
email_unverified
email_verified
campus_verified
```

Rules:

- Successful email-token login displays `email_verified`.
- Campus verified identity displays `campus_verified` preferentially.
- `email_unverified` is only shown for pending binding/progress contexts.
- Frontend does not infer badges from provider, email domain, or login method.

## 9. Membership

Phase 1 membership:

```text
premium
```

Visible behavior:

- Premium nickname is red.
- Premium badge is shown.

Rules:

- Active membership is required for red name and badge.
- Expired/revoked membership must not display benefits.
- Banned/deleted users must not display active membership benefits.
- Membership cannot affect moderation, admin permissions, organization permissions, or ranking unless a future PRD explicitly approves it.

## 10. Site Admin Accounts

Site admins use:

```text
admin_accounts
```

Rules:

- Every admin account binds to a normal user.
- Admins log in through normal auth/session.
- Access to `/api/v1/admin` requires active admin account.
- Revoking admin account does not delete the user.
- Admin writes must produce audit logs.

Admin roles:

```text
moderator
admin
super_admin
```

Capability examples:

```text
manage_users
ban_users
manage_boards
manage_tags
hide_content
restore_content
review_resources
resolve_reports
view_audit_logs
view_security_logs
manage_admin_accounts
```

Rules:

- Phase 1 maps admin role to capabilities in backend code.
- Do not build database-driven full RBAC in Phase 1.
- Security audit access can be a capability rather than a separate role.

## 11. Organizations

Organizations provide first-class identities for clubs, official departments, project groups, and later organization management.

Organization types:

```text
club
official_department
project_group
other
```

Organization statuses:

```text
active
suspended
archived
```

Rules:

- Organizations can own/display content through `contents.organization_id`.
- Organization status affects whether members can publish as that organization.
- Organization roles do not grant site admin authority.

## 12. Organization Membership And Multi-Role Model

Use:

```text
organization_members
organization_member_roles
```

Rules:

- One user may join many organizations.
- One user has one member row per organization.
- One member row may have many roles.
- Roles apply only within that organization.

Initial organization roles:

```text
owner
admin
recruiter
content_publisher
event_manager
member
```

Organization publishing permissions:

- `owner` can publish.
- `admin` can publish.
- `content_publisher` can publish.
- Other roles need explicit future PRD approval.

## 13. Authoring Model

Content records distinguish actual operator from displayed author.

Required fields on `contents`:

```text
author_user_id
author_display_type
organization_id
anonymous
```

Allowed `author_display_type`:

```text
user
organization
anonymous
```

Rules:

- `author_user_id` is always the real acting user.
- User-authored content displays the user.
- Organization-authored content displays the organization.
- Anonymous content hides the user in ordinary APIs.
- Admin APIs may reveal the real author only with permission.
- Publishing as organization requires active organization membership and publish-capable role.

## 14. Content Visibility

Allowed visibility values:

```text
public
login_required
campus_verified
organization_only
admin_only
```

Rules:

- Phase 1 primarily uses `public` and `login_required`.
- `campus_verified` is enabled after campus verification or manual campus mark.
- `organization_only` is reserved for organization module depth.
- `admin_only` is only for admin workflows.
- Backend enforces visibility for reads, search, file access, and comments.
- Elasticsearch index includes visibility, but backend search API performs final filtering.

## 15. Board Policy Interaction

Board policies affect publishing:

```text
open_publish
review_required
verified_only
admin_only
```

Rules:

- `open_publish` allows normal publish subject to login/board status.
- `review_required` creates or requires review flow.
- `verified_only` requires sufficient `auth_level`.
- `admin_only` requires admin account/capability.
- Anonymous publishing additionally requires `boards.allow_anonymous = true`.

## 16. Capability Computation

Backend computes capabilities from:

- Current session.
- User account status.
- Auth level.
- Admin account and admin capability map.
- Organization membership roles.
- Board policy.
- Content visibility and status.
- Content authoring fields.
- Review state.

Rules:

- Capability computation belongs on backend.
- Frontend uses capabilities only to render controls.
- Backend write APIs repeat authorization checks.

## 17. Audit Requirements

Audit logs are required for:

- Admin login or admin access failure.
- Admin account grant/revoke.
- User ban/unban.
- Manual auth-level changes.
- Organization role grant/revoke.
- Publishing as organization.
- Content hide/restore/delete.
- Resource review state change.
- Report resolution.
- Plaintext IP access through security views or security APIs.
