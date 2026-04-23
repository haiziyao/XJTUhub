# 数据模型规则

> **权威性说明：** 本文件是 `doc/backend/data-model-rules.md` 的中文镜像，只供人工阅读。AI agent 和实现工作必须以英文 `doc/` 为准。

MySQL 是 Phase 1 的事实数据源。

## 核心实体

最低逻辑实体：

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

## 统一内容模型

`contents` 存储所有内容类型共享的字段：

- id。
- type。
- board id。
- author id。
- title。
- body。
- status。
- visibility。
- anonymous display flag。
- pinned flag。
- view count。
- published time。
- created and updated times。

允许的内容类型：

- `post`
- `resource`
- `activity`
- `experience`
- `blog`
- `tool`

## 元数据规则

`content_metadata` 可以存储低频或早期阶段的类型专属字段。

示例：

- `resource.course_code`
- `resource.teacher`
- `activity.location`
- `activity.starts_at`
- `experience.direction`
- `tool.url`

不要把 `content_metadata` 变成高频业务字段的永久垃圾桶。当某类内容变得重要时，引入类型化详情表。

## 身份和角色字段

用户必须区分：

- role：治理和管理权限。
- auth level：身份可信等级。
- membership：展示和未来权益。

建议角色：

- `guest`
- `user`
- `verified_user`
- `moderator`
- `admin`
- `super_admin`

建议身份可信等级：

- `email_user`
- `campus_app_verified`
- `official_org`

## 会员字段

会员必须与用户角色分开存储：

- user id。
- membership type。
- status。
- start time。
- expiry time。
- source。

Phase 1 会员类型：

- `premium`

## 资源审核状态

资源审核状态值：

- `unreviewed`
- `verified`
- `rejected`
- `disputed`

后端控制状态。前端只展示返回值。

## 删除

用户生成内容、评论、附件和治理目标优先使用软删除。

硬删除需要文档化理由，并考虑审计、法律和滥用调查需要。
