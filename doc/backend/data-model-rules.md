# Data Model Rules

MySQL is the source of truth for Phase 1.

## Core Entities

Minimum logical entities:

- `users`
- `user_memberships`
- `boards`
- `contents`
- `content_metadata`
- `attachments`
- `comments`
- `tags`
- `content_tags`
- `content_reactions`
- `comment_reactions`
- `reports`
- `review_tasks`
- `notifications`
- `audit_logs`

## Unified Content Model

`contents` stores fields shared by all content types:

- id.
- type.
- board id.
- author id.
- title.
- body.
- status.
- visibility.
- anonymous display flag.
- pinned flag.
- view count.
- published time.
- created and updated times.

Allowed content types:

- `post`
- `resource`
- `activity`
- `experience`
- `blog`
- `tool`

## Metadata Rules

`content_metadata` may store low-frequency or early-stage type-specific fields.

Examples:

- `resource.course_code`
- `resource.teacher`
- `activity.location`
- `activity.starts_at`
- `experience.direction`
- `tool.url`

Do not turn `content_metadata` into a permanent dumping ground for high-frequency business fields. When a content type becomes important, introduce a typed detail table.

## Identity And Role Fields

Users must separate:

- role: moderation and administration permissions.
- auth level: identity trust.
- membership: display and future benefits.

Suggested roles:

- `guest`
- `user`
- `verified_user`
- `moderator`
- `admin`
- `super_admin`

Suggested auth levels:

- `email_user`
- `campus_app_verified`
- `official_org`

## Membership Fields

Membership must be stored separately from user role:

- user id.
- membership type.
- status.
- start time.
- expiry time.
- source.

Phase 1 membership type:

- `premium`

## Resource Review State

Resource review status values:

- `unreviewed`
- `verified`
- `rejected`
- `disputed`

The backend controls status. The frontend only displays returned values.

## Deletion

Prefer soft deletion for user-generated content, comments, attachments, and moderation targets.

Hard deletion requires a documented reason and must consider audit, legal, and abuse-investigation needs.
