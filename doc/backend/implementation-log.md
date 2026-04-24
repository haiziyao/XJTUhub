# 后端开发记录

本文档用于沉淀已经落地的后端实现批次，便于后续 agent 或人工接手时快速定位当前状态。

## 2026-04-24

### `Uncommitted object storage service MVP`

完成内容：
- 新增 `ObjectStorageService` 统一对象存储接口。
- 新增 `ObjectUploadRequest`、`PresignedObjectUrl`、`ObjectMetadata`。
- 新增 `MinioObjectStorageAdapter`，支持预签名上传/下载、删除、复制和读取元数据。
- 新增 `InMemoryObjectStorageService`，用于测试和本地无 MinIO 依赖场景。
- 新增 `FileStorageConfiguration`，存在 `MinioClient` 时优先使用 MinIO adapter。
- 补充文件存储模块文档和测试。

影响范围：
- `backend/src/main/java/org/xjtuhub/filestorage`
- `backend/src/test/java/org/xjtuhub/filestorage`
- `doc/backend/modules/file_storage/api.md`
- `doc/backend/storage-and-files.md`

### `Uncommitted admin user identity bindings query`

完成内容：
- 新增 `GET /api/v1/admin/users/{userId}/identity-bindings`。
- 返回指定用户身份绑定列表，供后台核验用户身份。
- 接口复用管理员登录与激活管理员账号校验。
- `primary` 与 `lastUsed` 由后端基于用户记录计算。
- 补充管理员成功查询与非管理员拒绝访问测试。

影响范围：
- `backend/src/main/java/org/xjtuhub/admin`
- `backend/src/test/java/org/xjtuhub/admin/AdminFlowTests.java`
- `doc/backend/modules/admin/api.md`
- `doc/shared/task-board.md`
- `doc/shared/task-completion-log.md`

### `Uncommitted campus scan session store`

完成内容：
- 新增 `CampusScanStore` 抽象，预留校园扫码登录会话读写能力。
- 新增 `InMemoryCampusScanStore` 和 `MybatisCampusScanStore`。
- 新增 `CampusAppLoginSessionEntity` / `CampusAppLoginSessionMapper`，映射 `campus_app_login_sessions`。
- 支持创建扫码会话、按 `sceneId` 查询、按二维码 token hash 查询、标记扫描、确认和取消。
- HTTP 扫码接口继续保持 `501 AUTH_CAMPUS_SCAN_RESERVED`，不接真实校园协议。

影响范围：
- `backend/src/main/java/org/xjtuhub/auth`
- `backend/src/main/java/org/xjtuhub/auth/persistence`
- `backend/src/test/java/org/xjtuhub/auth/CampusScanStoreTests.java`
- `backend/src/test/java/org/xjtuhub/auth/CampusScanStoreSelectionTests.java`
- `doc/backend/modules/auth/api.md`

### `Uncommitted campus verification history query`

完成内容：
- 新增 `GET /api/v1/admin/users/{userId}/campus-verification/history`，按目标用户查询手工校园认证标记历史。
- 基于 `audit_logs` 筛选 `action = admin_mark_campus_verification`、`target_type = user`、`target_id = userId`。
- `AdminStore` 增加 action/target 筛选分页读取与计数能力。
- MyBatis / 内存双实现均支持筛选查询。
- 补充管理员成功查询与非管理员拒绝访问测试。

影响范围：
- `backend/src/main/java/org/xjtuhub/admin`
- `backend/src/test/java/org/xjtuhub/admin/AdminFlowTests.java`
- `doc/backend/modules/admin/api.md`
- `doc/shared/task-board.md`
- `doc/shared/task-completion-log.md`

### `Uncommitted admin audit log list`

完成内容：
- 新增 `GET /api/v1/admin/audit-logs`，支持 `page` / `pageSize` 基础分页。
- 新增 `AdminAuditLogDto`，返回审计动作、对象、请求 ID、安全哈希、详情 JSON 与创建时间。
- `AdminStore` 增加审计日志分页读取与计数能力。
- MyBatis 实现读取 `audit_logs`，内存实现保存并返回审计记录，保持测试和本地回退路径可用。
- 补充管理员成功查询与非管理员拒绝访问测试。

影响范围：
- `backend/src/main/java/org/xjtuhub/admin`
- `backend/src/test/java/org/xjtuhub/admin/AdminFlowTests.java`
- `doc/backend/modules/admin/api.md`
- `doc/shared/task-board.md`
- `doc/shared/task-completion-log.md`

### `Uncommitted admin current identity query`

完成内容：
- 新增 `GET /api/v1/admin/me`，返回当前管理员账号 ID、用户 ID、角色和状态。
- 复用现有登录会话校验与 `admin_accounts.status = active` 管理员校验。
- 新增 `CurrentAdminResponse` DTO。
- 补充管理员成功查询与非管理员拒绝访问测试。

影响范围：
- `backend/src/main/java/org/xjtuhub/admin`
- `backend/src/test/java/org/xjtuhub/admin/AdminFlowTests.java`
- `doc/backend/modules/admin/api.md`
- `doc/shared/task-board.md`
- `doc/shared/task-completion-log.md`

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

### `Uncommitted SMTP email delivery integration`

完成内容：

- 引入 `spring-boot-starter-mail`，为邮箱验证码链路接入 Spring Mail。
- 新增 `SmtpEmailSender`，在存在 `JavaMailSender` 时通过 SMTP 真实发信。
- `EmailSender` 选择策略调整为：
  - SMTP 可用时使用 `SmtpEmailSender`
  - 未配置邮件客户端时回退到 `LoggingEmailSender`
- 新增定向测试：
  - `SmtpEmailSenderSelectionTests`
  - `EmailTokenRateLimitTests` 的真实 `MimeMessage` mock/stub 修复
- 环境变量模板补充邮件配置项，支持 QQ 邮箱 SMTP。

影响范围：

- `backend/src/main/java/org/xjtuhub/auth`
- `backend/src/main/resources/application.yml`
- `.env.example`
- `backend/README.md`
- `doc/backend/modules/auth/api.md`

### `Uncommitted six-digit email verification code`

完成内容：

- 邮箱登录验证码从内部长 token 调整为固定 6 位数字验证码。
- 登录邮件标题和文案统一改为“验证码”语义，不再向用户暴露内部 token 风格字符串。
- 会话 token 生成同步改为基于 `SecureRandom` 的随机字节十六进制串。
- 增加验证码格式测试，防止回退。

影响范围：

- `backend/src/main/java/org/xjtuhub/auth/AuthService.java`
- `backend/src/test/java/org/xjtuhub/AuthFlowTests.java`
- `doc/backend/modules/auth/api.md`

### `Uncommitted redis-backed email verification codes`

完成内容：

- 邮箱验证码活跃态新增 `EmailVerificationCodeStore` 抽象。
- Redis 环境下使用 `RedisEmailVerificationCodeStore`，key 前缀固定为 `xjtuhub:auth:email-code:`。
- 验证码 Redis TTL 固定为 5 分钟；无 Redis 时回退到内存实现，方便测试与本地开发。
- 数据库中的 `email_verification_tokens` 继续保留审计和状态轨迹，避免丢失已使用/已过期语义。
- 邮件内容改为正式验证码通知模板。

影响范围：

- `backend/src/main/java/org/xjtuhub/auth`
- `backend/src/test/java/org/xjtuhub/auth`
- `backend/README.md`
- `doc/backend/modules/auth/api.md`

### `Uncommitted auth store selection fix`

完成内容：

- 修复 `AuthStore` 在真实运行环境中误回退到 `InMemoryAuthStore` 的实现选择问题。
- `AuthStore` 选择改为显式配置：
  - 存在完整 MyBatis 依赖时使用 `MybatisAuthStore`
  - 否则回退到共享的 `InMemoryAuthStore`
- 增加 `AuthStoreSelectionTests`，防止后续再次误回退。
- 真实链路已验证：邮箱验证码发送后会写入 MySQL 审计表，登录闭环成功。

影响范围：

- `backend/src/main/java/org/xjtuhub/auth`
- `backend/src/test/java/org/xjtuhub/auth/AuthStoreSelectionTests.java`
- `doc/backend/modules/auth/api.md`

### `Uncommitted admin campus verification MVP`

完成内容：

- `admin` 模块落地第一条可执行后台能力：
  - `POST /api/v1/admin/users/{userId}/campus-verification`
- 新增 `AdminStore` 抽象以及 MyBatis / 内存双实现。
- 接口要求：
  - 当前用户已登录
  - 当前用户在 `admin_accounts` 中存在激活态记录
- 成功后会：
  - 将目标用户 `auth_level` 更新为 `campus_app_verified`
  - 写入或更新 `campus_app` 身份绑定
  - 写入 `audit_logs`
- 增加端到端测试：
  - 管理员成功标记校园认证
  - 非管理员访问被拒绝

影响范围：

- `backend/src/main/java/org/xjtuhub/admin`
- `backend/src/test/java/org/xjtuhub/admin/AdminFlowTests.java`
- `doc/backend/modules/admin/api.md`

### `Uncommitted search placeholder skeleton`

完成内容：

- `search` 模块新增可运行 API 骨架：
  - `GET /api/v1/search`
  - `GET /api/v1/search/index-tasks`
- 搜索接口当前返回稳定空结果，供前端联调和契约测试使用。
- 索引任务接口当前返回固定占位任务状态，供后台页和后续 ES 接入前联调使用。
- 新增 `SearchFlowTests`。
- 新增中文模块接口文档。

影响范围：

- `backend/src/main/java/org/xjtuhub/search`
- `backend/src/test/java/org/xjtuhub/search/SearchFlowTests.java`
- `doc/backend/modules/search/api.md`

## 当前建议接力点

按优先级建议后续继续实现：

1. 校园认证真实接入前的 `adapter` 抽象与保留数据读写层。
2. `auth/user` 的管理视角查询接口，例如登录事件分页、会话分页、身份绑定查询。
3. `admin` 模块最小骨架，先支持手工标记 `campus_app_verified` 的高信任流程。
