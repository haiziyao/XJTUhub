# Admin 模块接口文档

本文档描述当前已经实现的 `admin` 模块接口。

基础路径：

```text
/api/v1/admin
```

全局响应包络、统一错误结构继承自：

- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/prd/global-auth-organization-permission.md`

## 1. 手工标记校园认证保留接口

```text
POST /api/v1/admin/users/{userId}/campus-verification
```

用途：

- 为后续后台人工标记 `campus_app_verified` 预留接口边界。

当前行为：

- 当前阶段不实现真实后台逻辑。
- 统一返回 `501 Not Implemented`。
- 统一错误码为 `ADMIN_CAMPUS_VERIFICATION_RESERVED`。

路径参数规则：

- `userId` 必须是数字字符串。

当前错误码：

- `VALIDATION_FAILED`
- `ADMIN_CAMPUS_VERIFICATION_RESERVED`

## 2. 当前实现说明

- 当前 `admin` 模块只建立了第一批接口骨架。
- 真正的后台鉴权、`admin_accounts` 校验、审计日志、手工标记 `campus_app_verified` 的写库逻辑将在后续批次落地。
