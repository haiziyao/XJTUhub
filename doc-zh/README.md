# XJTUhub 文档地图（中文镜像）

> **权威性说明：** `doc-zh/` 只供人工阅读。AI agent、实现工作和代码评审必须读取英文 `doc/`。如果 `doc/` 与 `doc-zh/` 不一致，以 `doc/` 为准，并在之后同步更新 `doc-zh/`。

XJTUhub 使用文档先行开发。本地图说明每类决策应该放在哪里。

## 阅读顺序

1. 从 `../Agent.md` 开始，了解 agent 和贡献者规则。
2. 修改范围前阅读产品约束。
3. 定义 API、数据、权限、存储、搜索或治理前阅读后端文档。
4. 设计页面、生成视觉提示词或编写 Nuxt 代码前阅读前端文档。
5. 将长期决策记录到 `shared/decision-log.md`。

## 产品文档

| 文档 | 用途 |
| --- | --- |
| `product/product-constraints.md` | 产品边界、身份、会员、匿名内容、治理和资源可信标识。 |
| `product/phase-roadmap.md` | Phase 1 MVP 和后续扩展路线。 |
| `product/information-architecture.md` | 板块、标签、导航和统一内容类型。 |

## 后端文档

| 文档 | 用途 |
| --- | --- |
| `backend/backend-architecture.md` | Spring Boot 模块化单体边界和未来 Axum 拆分原则。 |
| `backend/backend-prd-rules.md` | 后端 PRD 的必需结构。 |
| `backend/data-model-rules.md` | 逻辑实体、统一内容模型、身份、会员和审核状态。 |
| `backend/api-contracts.md` | API 响应、错误、分页、版本和前端集成规则。 |
| `backend/storage-and-files.md` | 对象存储抽象、MinIO 适配器、未来 OSS/COS 适配器和附件策略。 |
| `backend/search.md` | Elasticsearch 的事实源边界、索引和搜索 API 规则。 |
| `backend/security-and-moderation.md` | 登录、权限、治理、匿名、举报、点踩、审计和限流。 |
| `backend/testing.md` | 后端测试期望和高风险覆盖区域。 |

## 前端文档

| 文档 | 用途 |
| --- | --- |
| `frontend/frontend-workflow.md` | 后端 PRD -> 页面 spec -> 提示词 -> gpt-image-2 参考图 -> Nuxt 实现流程。 |
| `frontend/ui-design-rules.md` | SSR/CSR 策略、SEO、必需 UI 状态和可访问性约束。 |
| `frontend/page-spec-rules.md` | 前端页面 spec 的必需结构。 |
| `frontend/image-prompt-rules.md` | 生成和使用视觉参考提示词的规则。 |
| `frontend/implementation-rules.md` | Nuxt 实现、API client、权限、组件和视觉参考规则。 |

## 共享文档

| 文档 | 用途 |
| --- | --- |
| `shared/glossary.md` | 产品、后端和前端共享术语。 |
| `shared/decision-log.md` | 长期决策和原因。 |
| `shared/dev-workflow.md` | 文档先行流程、隔离规则和评审指导。 |

## 归属规则

当一条规则同时涉及前后端时，应将权威版本放在负责该规则的区域，并在其他位置交叉引用。除非有意保持同步，否则不要在多个地方复制完整规则。
