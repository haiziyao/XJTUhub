# 后端开发记录

本文档用于沉淀已经落地的后端实现批次，便于后续 agent 或人工接手时快速定位当前状态。

## 2026-04-24

### `2d23e5b Migrate auth persistence to mybatis plus`

完成内容：

- `auth` 模块业务持久化从 `JdbcTemplate` 迁移到 `MyBatis + MyBatis-Plus`。
- 新增 `entity` / `mapper` 基础结构。
- `DependencyHealthService` 的 MySQL 探测改为基于 `DataSource`。
- 测试环境继续保留内存存储回退路径。

影响范围：

- `backend/src/main/java/org/xjtuhub/auth`
- `backend/src/main/java/org/xjtuhub/system`
- `doc/backend/modules/auth/api.md`

### `a6996f1 Enhance auth session activity and profile validation`

完成内容：

- 会话列表和登录响应新增 `lastSeenAt`。
- 鉴权通过时刷新会话最近活跃时间。
- 邮箱验证码校验失败增加限流。
- 用户资料更新新增 `avatarUrl` 的 `http/https` URL 校验。

影响范围：

- `auth` 模块会话与验证码逻辑。
- `user` 模块资料更新校验。
- `doc/backend/modules/auth/api.md`
- `doc/backend/modules/user/api.md`

### `6507e9e Add identity bindings and reserved campus scan APIs`

完成内容：

- `CurrentUserDto` 新增：
  - `identitySummary`
  - `identityBindings`
- 当前用户接口支持返回身份绑定摘要，供前端在昵称后展示身份来源。
- 预留校园扫码登录接口：
  - `POST /api/v1/auth/campus-scan/sessions`
  - `GET /api/v1/auth/campus-scan/sessions/{sceneId}`
  - `POST /api/v1/auth/campus-scan/sessions/{sceneId}/confirm`
- 当前阶段上述接口统一返回 `501` 和错误码 `AUTH_CAMPUS_SCAN_RESERVED`。

影响范围：

- `auth` 模块当前用户展示数据。
- 校园扫码登录保留路由骨架。
- `doc/backend/modules/auth/api.md`
- `doc/backend/modules/user/api.md`

### `7facca2 Persist auth session security metadata`

完成内容：

- 登录会话创建时写入：
  - `sessions.ip_address`
  - `sessions.ip_hash`
  - `sessions.user_agent_hash`
- 邮箱验证码校验限流切换为：
  - Redis 优先
  - 内存兜底
- 增加定向测试：
  - 会话安全元信息写库测试
  - Redis 优先选择测试

影响范围：

- `auth` 模块登录会话安全元信息。
- 验证码校验限流存储选择。
- `backend/README.md`
- `doc/backend/modules/auth/api.md`

## 当前建议接力点

按优先级建议后续继续实现：

1. 校园认证真实接入前的 `adapter` 抽象与保留数据读写层。
2. `auth/user` 的管理视角查询接口，例如登录事件分页、会话分页、身份绑定查询。
3. `admin` 模块最小骨架，先支持手工标记 `campus_app_verified` 的高信任流程。
