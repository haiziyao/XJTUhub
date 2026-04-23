# Global Data Model PRD

This document defines the logical model and suggested MySQL physical model for the XJTUhub backend foundation.

It is not a migration file. Implementation agents must translate this design into migrations according to the chosen Spring Boot migration tool.

## 1. Physical Conventions

### 1.1 Table And Column Style

- Table names: plural `snake_case`.
- Column names: `snake_case`.
- Enum values: `lower_snake_case`.
- Primary key: `id bigint not null primary key`.
- No MySQL `FOREIGN KEY` constraints in Phase 1.
- Logical foreign keys must still be indexed.

### 1.2 Time And Audit

Business tables usually include:

```sql
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Append-only event/log tables usually include only:

```sql
created_at datetime(3) not null
```

All `datetime(3)` values are stored in UTC.

### 1.3 Charset

Recommended table default:

```sql
default character set utf8mb4 collate utf8mb4_unicode_ci
```

### 1.4 High-Sensitive Fields

Plaintext IP fields are high-sensitive:

```text
ip_address
```

Rules:

- Ordinary APIs must not return plaintext IP.
- Admin APIs must not return plaintext IP by default.
- Security audit access requires explicit capability.
- MySQL account/view separation should prevent accidental plaintext IP reads.

## 2. Logical Domain Map

| Domain | Tables |
| --- | --- |
| User/auth | `users`, `user_auth_identities`, `email_verification_tokens`, `sessions`, `user_login_events` |
| Membership | `user_memberships` |
| Admin | `admin_accounts` |
| Organization | `organizations`, `organization_members`, `organization_member_roles` |
| Content | `boards`, `tags`, `contents`, `resource_details`, `activity_details`, `content_metadata`, `content_tags` |
| Comment | `comments` |
| Reaction | `content_reactions`, `comment_reactions` |
| Files | `attachments`, `file_download_logs` |
| Moderation | `reports`, `review_tasks` |
| Notification | `notifications`, `notification_deliveries` |
| Audit | `audit_logs` |
| Search | `search_index_tasks` |
| Reserved auth | `campus_app_login_sessions` |

## 3. User And Auth Tables

### 3.1 `users`

Purpose: stable account and profile.

```sql
id bigint not null primary key,
nickname varchar(64) not null,
avatar_url varchar(512) null,
bio varchar(512) null,
account_status varchar(32) not null default 'active',
auth_level varchar(32) not null default 'email_user',
primary_identity_provider varchar(32) null,
last_login_provider varchar(32) null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
index idx_users_status (account_status),
index idx_users_auth_level (auth_level),
index idx_users_deleted_at (deleted_at)
```

Allowed `account_status`:

```text
active
banned
disabled
deleted
```

Allowed `auth_level`:

```text
email_user
campus_app_verified
official_org
```

### 3.2 `user_auth_identities`

Purpose: provider identities bound to users.

```sql
id bigint not null primary key,
user_id bigint not null,
provider varchar(32) not null,
provider_subject varchar(255) not null,
provider_display varchar(255) null,
verification_status varchar(32) not null default 'unverified',
verified_at datetime(3) null,
last_used_at datetime(3) null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
unique key uk_auth_provider_subject (provider, provider_subject),
index idx_auth_user_id (user_id),
index idx_auth_provider_status (provider, verification_status)
```

Allowed `provider`:

```text
email
campus_app
```

Allowed `verification_status`:

```text
unverified
verified
revoked
```

### 3.3 `email_verification_tokens`

Purpose: single-use email login/binding tokens.

```sql
id bigint not null primary key,
email varchar(255) not null,
token_hash varchar(128) not null,
purpose varchar(32) not null,
status varchar(32) not null default 'active',
expires_at datetime(3) not null,
consumed_at datetime(3) null,
created_at datetime(3) not null
```

Indexes:

```sql
unique key uk_email_token_hash (token_hash),
index idx_email_token_email_purpose (email, purpose),
index idx_email_token_expires_at (expires_at)
```

Allowed `purpose`:

```text
register
login
bind_email
```

Password reset is intentionally excluded.

### 3.4 `sessions`

Purpose: HttpOnly Cookie login sessions.

```sql
id bigint not null primary key,
user_id bigint not null,
session_token_hash varchar(128) not null,
status varchar(32) not null default 'active',
login_provider varchar(32) not null,
device_label varchar(128) null,
ip_address varchar(45) null,
ip_hash varchar(128) null,
user_agent_hash varchar(128) null,
expires_at datetime(3) not null,
last_seen_at datetime(3) null,
created_at datetime(3) not null,
updated_at datetime(3) not null
```

Indexes:

```sql
unique key uk_sessions_token_hash (session_token_hash),
index idx_sessions_user_status (user_id, status),
index idx_sessions_expires_at (expires_at),
index idx_sessions_ip_hash (ip_hash)
```

Allowed `status`:

```text
active
revoked
expired
```

### 3.5 `user_login_events`

Purpose: append-only login attempt events.

```sql
id bigint not null primary key,
user_id bigint null,
provider varchar(32) not null,
event_type varchar(32) not null,
success tinyint(1) not null,
failure_reason varchar(64) null,
ip_address varchar(45) null,
ip_hash varchar(128) null,
user_agent_hash varchar(128) null,
created_at datetime(3) not null
```

Indexes:

```sql
index idx_login_user_time (user_id, created_at),
index idx_login_ip_hash_time (ip_hash, created_at),
index idx_login_success_time (success, created_at)
```

## 4. Membership And Admin Tables

### 4.1 `user_memberships`

Purpose: premium display and future benefit records.

```sql
id bigint not null primary key,
user_id bigint not null,
membership_type varchar(32) not null,
status varchar(32) not null,
started_at datetime(3) not null,
expires_at datetime(3) null,
source varchar(64) null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
index idx_memberships_user_status (user_id, status),
index idx_memberships_type_status (membership_type, status)
```

Allowed `membership_type`:

```text
premium
```

Allowed `status`:

```text
active
expired
revoked
```

### 4.2 `admin_accounts`

Purpose: site-level admin authority bound to normal users.

```sql
id bigint not null primary key,
user_id bigint not null,
admin_role varchar(32) not null,
status varchar(32) not null default 'active',
granted_by bigint null,
granted_at datetime(3) not null,
revoked_by bigint null,
revoked_at datetime(3) null,
revoke_reason varchar(255) null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
index idx_admin_user_status (user_id, status),
index idx_admin_role_status (admin_role, status)
```

Do not use `unique(user_id, status)` because multiple historical revoked/suspended rows may exist. Enforce one active admin account per user in application logic or a future partial-index-compatible strategy.

Allowed `admin_role`:

```text
moderator
admin
super_admin
```

Allowed `status`:

```text
active
revoked
suspended
```

## 5. Organization Tables

### 5.1 `organizations`

Purpose: club/department/project identity.

```sql
id bigint not null primary key,
name varchar(128) not null,
slug varchar(128) not null,
type varchar(32) not null,
description text null,
avatar_url varchar(512) null,
status varchar(32) not null default 'active',
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
unique key uk_organizations_slug (slug),
index idx_organizations_type_status (type, status)
```

Allowed `type`:

```text
club
official_department
project_group
other
```

### 5.2 `organization_members`

Purpose: one user's membership in one organization.

```sql
id bigint not null primary key,
organization_id bigint not null,
user_id bigint not null,
status varchar(32) not null default 'active',
joined_at datetime(3) null,
left_at datetime(3) null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
unique key uk_org_member_user (organization_id, user_id),
index idx_org_members_user_status (user_id, status),
index idx_org_members_org_status (organization_id, status)
```

Allowed `status`:

```text
active
invited
left
removed
```

### 5.3 `organization_member_roles`

Purpose: multiple roles for a member inside one organization.

```sql
id bigint not null primary key,
member_id bigint not null,
role varchar(32) not null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
unique key uk_org_member_role (member_id, role),
index idx_org_member_roles_role (role)
```

Allowed `role`:

```text
owner
admin
recruiter
content_publisher
event_manager
member
```

## 6. Board, Tag, And Content Tables

### 6.1 `boards`

Purpose: content containers and posting policies.

```sql
id bigint not null primary key,
parent_id bigint null,
slug varchar(128) not null,
name varchar(128) not null,
description varchar(512) null,
visibility varchar(32) not null default 'public',
post_policy varchar(32) not null default 'open_publish',
allow_anonymous tinyint(1) not null default 0,
sort_order int not null default 0,
status varchar(32) not null default 'active',
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
unique key uk_boards_slug (slug),
index idx_boards_parent_sort (parent_id, sort_order),
index idx_boards_status (status)
```

Allowed `post_policy`:

```text
open_publish
review_required
verified_only
admin_only
```

### 6.2 `tags`

Purpose: shared labels.

```sql
id bigint not null primary key,
name varchar(64) not null,
slug varchar(64) not null,
category varchar(64) null,
status varchar(32) not null default 'active',
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
unique key uk_tags_slug (slug),
index idx_tags_category_status (category, status)
```

### 6.3 `contents`

Purpose: unified content table.

```sql
id bigint not null primary key,
type varchar(32) not null,
board_id bigint not null,
author_user_id bigint not null,
author_display_type varchar(32) not null default 'user',
organization_id bigint null,
anonymous tinyint(1) not null default 0,
title varchar(200) not null,
body mediumtext null,
status varchar(32) not null default 'draft',
visibility varchar(32) not null default 'public',
pinned tinyint(1) not null default 0,
view_count bigint not null default 0,
published_at datetime(3) null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
index idx_contents_board_status_time (board_id, status, published_at),
index idx_contents_author_time (author_user_id, created_at),
index idx_contents_org_time (organization_id, created_at),
index idx_contents_type_status_time (type, status, published_at),
index idx_contents_visibility_status (visibility, status),
index idx_contents_deleted_at (deleted_at)
```

Allowed `type`:

```text
post
resource
activity
experience
blog
tool
```

Allowed `author_display_type`:

```text
user
organization
anonymous
```

Allowed `status`:

```text
draft
pending_review
published
hidden
rejected
archived
deleted
```

Allowed `visibility`:

```text
public
login_required
campus_verified
organization_only
admin_only
```

### 6.4 `resource_details`

Purpose: structured resource fields.

```sql
id bigint not null primary key,
content_id bigint not null,
course_code varchar(64) null,
course_name varchar(128) null,
teacher_name varchar(128) null,
semester varchar(64) null,
resource_type varchar(64) null,
review_status varchar(32) not null default 'unreviewed',
download_policy varchar(32) not null default 'allow',
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
unique key uk_resource_details_content (content_id),
index idx_resource_course (course_code, course_name),
index idx_resource_review_status (review_status)
```

Allowed `review_status`:

```text
unreviewed
verified
rejected
disputed
```

### 6.5 `activity_details`

Purpose: structured activity fields.

```sql
id bigint not null primary key,
content_id bigint not null,
starts_at datetime(3) null,
ends_at datetime(3) null,
location varchar(255) null,
registration_url varchar(512) null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
unique key uk_activity_details_content (content_id),
index idx_activity_starts_at (starts_at)
```

### 6.6 `content_metadata`

Purpose: low-frequency extension fields.

```sql
id bigint not null primary key,
content_id bigint not null,
meta_key varchar(128) not null,
meta_value text null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
unique key uk_content_meta_key (content_id, meta_key),
index idx_content_meta_key (meta_key)
```

Rule: do not use `content_metadata` as a permanent dumping ground for high-frequency fields.

### 6.7 `content_tags`

Purpose: content-tag mapping.

```sql
content_id bigint not null,
tag_id bigint not null,
created_at datetime(3) not null,
created_by bigint null,
primary key (content_id, tag_id)
```

Indexes:

```sql
index idx_content_tags_tag (tag_id)
```

## 7. Comment And Reaction Tables

### 7.1 `comments`

Purpose: two-level comments.

```sql
id bigint not null primary key,
content_id bigint not null,
parent_id bigint null,
root_id bigint null,
author_user_id bigint not null,
reply_to_user_id bigint null,
anonymous tinyint(1) not null default 0,
body text not null,
status varchar(32) not null default 'published',
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
index idx_comments_content_time (content_id, created_at),
index idx_comments_root_time (root_id, created_at),
index idx_comments_author_time (author_user_id, created_at),
index idx_comments_status (status)
```

Rules:

- Top-level comment: `parent_id = null`.
- Reply: `parent_id` points to replied comment.
- `root_id` points to the top-level comment.
- UI must not create third-level nesting.

### 7.2 `content_reactions`

```sql
id bigint not null primary key,
content_id bigint not null,
user_id bigint not null,
reaction_type varchar(32) not null,
created_at datetime(3) not null
```

Indexes:

```sql
unique key uk_content_reaction (content_id, user_id, reaction_type),
index idx_content_reactions_user (user_id, created_at)
```

Allowed `reaction_type`:

```text
like
dislike
favorite
```

### 7.3 `comment_reactions`

```sql
id bigint not null primary key,
comment_id bigint not null,
user_id bigint not null,
reaction_type varchar(32) not null,
created_at datetime(3) not null
```

Indexes:

```sql
unique key uk_comment_reaction (comment_id, user_id, reaction_type),
index idx_comment_reactions_user (user_id, created_at)
```

Allowed `reaction_type`:

```text
like
dislike
```

## 8. File Tables

### 8.1 `attachments`

Purpose: object storage metadata.

```sql
id bigint not null primary key,
content_id bigint not null,
uploader_user_id bigint not null,
storage_provider varchar(32) not null,
bucket varchar(128) not null,
object_key varchar(512) not null,
file_name varchar(255) not null,
mime_type varchar(128) null,
size_bytes bigint not null,
checksum varchar(128) null,
visibility varchar(32) not null default 'inherit',
review_status varchar(32) not null default 'unreviewed',
file_status varchar(32) not null default 'active',
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
index idx_attachments_content (content_id),
index idx_attachments_uploader_time (uploader_user_id, created_at),
index idx_attachments_review_status (review_status),
index idx_attachments_object (storage_provider, bucket, object_key)
```

Allowed `storage_provider`:

```text
minio
aliyun_oss
tencent_cos
```

### 8.2 `file_download_logs`

Purpose: append-only resource download logs.

```sql
id bigint not null primary key,
attachment_id bigint not null,
content_id bigint not null,
user_id bigint null,
ip_address varchar(45) null,
ip_hash varchar(128) null,
user_agent_hash varchar(128) null,
review_status_at_download varchar(32) not null,
created_at datetime(3) not null
```

Indexes:

```sql
index idx_download_attachment_time (attachment_id, created_at),
index idx_download_content_time (content_id, created_at),
index idx_download_user_time (user_id, created_at),
index idx_download_ip_hash_time (ip_hash, created_at)
```

Recommended views:

```text
v_file_download_logs_public
v_file_download_logs_security
```

Rules:

- `v_file_download_logs_public` excludes `ip_address`.
- `v_file_download_logs_security` includes `ip_address`.
- Access to the security view requires separate database and application authorization.

## 9. Moderation Tables

### 9.1 `reports`

```sql
id bigint not null primary key,
target_type varchar(32) not null,
target_id bigint not null,
reporter_user_id bigint not null,
reason varchar(64) not null,
detail text null,
status varchar(32) not null default 'open',
resolved_by bigint null,
resolved_at datetime(3) null,
resolution_note text null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
index idx_reports_target_status (target_type, target_id, status),
index idx_reports_reporter_time (reporter_user_id, created_at),
index idx_reports_status_time (status, created_at)
```

Allowed `target_type`:

```text
content
comment
attachment
user
organization
```

### 9.2 `review_tasks`

```sql
id bigint not null primary key,
target_type varchar(32) not null,
target_id bigint not null,
review_type varchar(32) not null,
status varchar(32) not null default 'pending',
assigned_to bigint null,
reviewer_user_id bigint null,
note text null,
reviewed_at datetime(3) null,
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
index idx_review_target (target_type, target_id),
index idx_review_status_time (status, created_at),
index idx_review_assigned (assigned_to, status)
```

## 10. Notification Tables

### 10.1 `notifications`

```sql
id bigint not null primary key,
target_user_id bigint not null,
actor_user_id bigint null,
type varchar(64) not null,
title varchar(200) not null,
body varchar(1000) null,
related_content_id bigint null,
related_comment_id bigint null,
status varchar(32) not null default 'unread',
created_at datetime(3) not null,
updated_at datetime(3) not null,
deleted_at datetime(3) null,
created_by bigint null,
updated_by bigint null
```

Indexes:

```sql
index idx_notifications_user_status_time (target_user_id, status, created_at),
index idx_notifications_type_time (type, created_at)
```

### 10.2 `notification_deliveries`

```sql
id bigint not null primary key,
notification_id bigint not null,
channel varchar(32) not null,
delivery_status varchar(32) not null default 'pending',
provider_message_id varchar(255) null,
error_message varchar(1000) null,
sent_at datetime(3) null,
created_at datetime(3) not null,
updated_at datetime(3) not null
```

Indexes:

```sql
index idx_delivery_notification (notification_id),
index idx_delivery_status_time (delivery_status, created_at)
```

Allowed `channel`:

```text
site
email
```

## 11. Audit And Search Tables

### 11.1 `audit_logs`

Append-only sensitive operation logs.

```sql
id bigint not null primary key,
actor_user_id bigint null,
admin_account_id bigint null,
action varchar(128) not null,
target_type varchar(64) null,
target_id bigint null,
request_id varchar(64) null,
ip_address varchar(45) null,
ip_hash varchar(128) null,
user_agent_hash varchar(128) null,
details_json json null,
created_at datetime(3) not null
```

Indexes:

```sql
index idx_audit_actor_time (actor_user_id, created_at),
index idx_audit_target_time (target_type, target_id, created_at),
index idx_audit_action_time (action, created_at),
index idx_audit_request_id (request_id)
```

### 11.2 `search_index_tasks`

Retryable Elasticsearch indexing tasks.

```sql
id bigint not null primary key,
target_type varchar(32) not null,
target_id bigint not null,
operation varchar(32) not null,
status varchar(32) not null default 'pending',
attempt_count int not null default 0,
last_error varchar(1000) null,
next_retry_at datetime(3) null,
created_at datetime(3) not null,
updated_at datetime(3) not null
```

Indexes:

```sql
index idx_search_tasks_status_retry (status, next_retry_at),
index idx_search_tasks_target (target_type, target_id)
```

Allowed `operation`:

```text
upsert
delete
reindex
```

## 12. Reserved Campus Login Table

### 12.1 `campus_app_login_sessions`

Reserved for future campus official APP scan login.

```sql
id bigint not null primary key,
scene_id varchar(128) not null,
qr_token_hash varchar(128) not null,
status varchar(32) not null default 'pending',
matched_user_id bigint null,
expires_at datetime(3) not null,
scanned_at datetime(3) null,
confirmed_at datetime(3) null,
canceled_at datetime(3) null,
created_at datetime(3) not null,
updated_at datetime(3) not null
```

Indexes:

```sql
unique key uk_campus_scene_id (scene_id),
unique key uk_campus_qr_token_hash (qr_token_hash),
index idx_campus_status_expiry (status, expires_at)
```

Allowed `status`:

```text
pending
scanned
confirmed
expired
canceled
```

This table is reserved. Do not implement production scan-login behavior until the real campus protocol is known.

## 13. Data Model Review Checklist

Before implementing migrations, verify:

- Every lookup foreign-key-like column has an index.
- No MySQL `FOREIGN KEY` constraints are created.
- All API-visible IDs remain strings.
- Soft-delete columns exist on business tables.
- Append-only tables do not accidentally support ordinary soft deletion.
- Plaintext IP fields are isolated from ordinary DTOs.
- Membership, admin authority, and organization authority remain separate.
