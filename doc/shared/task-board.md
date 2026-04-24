# 多 Agent 任务表

本文档是 XJTUhub 的跨 agent 协作总任务表。

用途：

- 作为当前阶段唯一的跨 agent 任务台账。
- 记录任务状态、负责人、依赖、交付物和交接要求。
- 让后续 agent 先看任务表，再进入具体模块文档和代码。

使用规则：

- 新任务先登记到这里，再开始实现。
- 每个任务必须有唯一任务编号。
- 每个任务只能有一个当前主负责人。
- 被阻塞时必须写清阻塞原因和解除条件。
- 完成后不要删除任务，状态改为 `done`，并在 `task-completion-log.md` 追加完成记录。
- 如果任务拆分为子任务，子任务继续使用新的任务编号，不要在同一行里塞过多内容。

状态枚举：

- `todo`：已确认，但尚未开始。
- `in_progress`：正在执行。
- `blocked`：存在外部依赖或前置条件，当前不能推进。
- `review`：代码或文档已完成，等待验收/接力。
- `done`：已完成，并已写入完成记录。

优先级枚举：

- `P0`：阻塞主链路或基础设施。
- `P1`：当前阶段核心能力。
- `P2`：重要但不阻塞当前闭环。
- `P3`：可顺延事项。

## 当前任务表

| 任务 ID | 模块 | 任务名称 | 优先级 | 状态 | 当前负责人 | 前置依赖 | 交付物 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| T-001 | auth | 邮箱验证码登录闭环 | P0 | done | codex | MySQL / Redis / SMTP 可用 | `auth` 接口、测试、中文文档 | 已完成真实链路验证 |
| T-002 | auth | `AuthStore` 持久层选择修复 | P0 | done | codex | T-001 | 显式配置选择、选择测试 | 已修复误回退到内存实现 |
| T-003 | admin | 手工标记校园认证 MVP | P1 | done | codex | T-001、T-002 | `admin` 接口、审计写入、测试、中文文档 | 已落地最小可执行后台能力 |
| T-004 | admin | 管理员当前身份查询 | P1 | done | codex | T-003 | `GET /api/v1/admin/me`、DTO、中文文档、测试 | 已返回当前管理员账户与角色信息 |
| T-005 | admin | 管理员操作审计列表 | P1 | done | codex | T-003 | `GET /api/v1/admin/audit-logs`、分页 DTO、中文文档、测试 | 已完成管理员视角基础分页列表，不做复杂过滤 |
| T-006 | admin | 校园认证手工标记历史查询 | P1 | done | codex | T-003、T-005 | `GET /api/v1/admin/users/{userId}/campus-verification/history`、中文文档、测试 | 已按用户查询手工标记历史 |
| T-007 | auth | 校园扫码登录 adapter 预留数据层 | P1 | done | codex | T-001 | `campus_app_login_sessions` 读写层、中文文档、测试 | 已补数据层，不接真实校园协议 |
| T-008 | user | 用户身份绑定管理查询 | P2 | done | codex | T-001 | `GET /api/v1/admin/users/{userId}/identity-bindings`、中文文档、测试 | 已完成管理员按用户查询身份绑定 |
| T-009 | file_storage | 对象存储统一接口 MVP | P1 | done | codex | 后端存储文档已确认 | `ObjectStorageService`、MinIO 实现、中文文档、测试 | 已完成统一接口 MVP，兼容未来 OSS/COS adapter |
| T-010 | search | Elasticsearch 接入骨架 | P2 | blocked | codex | ES 环境部署完成 | 搜索 adapter、索引任务骨架、中文文档 | 搜索 API 占位骨架已完成，真实 ES 接入仍阻塞于运行环境 |

## 分配约定

- `codex`：当前主线程 agent。
- `unassigned`：尚未分配。
- 如果后续引入更多 agent，直接在“当前负责人”列写 agent 名称。

## 接力规则

接手任务前必须至少阅读：

1. 本文档。
2. 对应模块的 `doc/backend/modules/<module>/api.md`。
3. 对应 PRD 或架构文档。
4. `doc/backend/implementation-log.md` 最近相关批次。

开始接手任务时，必须先做两件事：

1. 将任务状态改为 `in_progress`。
2. 在备注里补充本次接手范围，避免多 agent 重叠修改。
