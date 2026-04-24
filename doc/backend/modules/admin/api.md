# Admin 模块接口文档

本文档描述当前已经实现的 `admin` 模块接口。

基础路径：

```text
/api/v1/admin
```

全局响应包络、统一错误结构继承自：

- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/prd/global-auth-organization-permission.md`

## 1. 手工标记校园认证

```text
POST /api/v1/admin/users/{userId}/campus-verification
```

用途：

- 由已登录管理员手工标记目标用户为 `campus_app_verified`。

请求体：

```json
{
  "note": "manual verification"
}
```

字段说明：

- `note` 可选，最长 255 字符，用于写入后台审计日志。

路径参数规则：

- `userId` 必须是数字字符串。

成功响应 `data`：

```json
{
  "userId": "1776993968755001",
  "authLevel": "campus_app_verified",
  "verificationStatus": "verified",
  "verifiedAt": "2026-04-24T01:26:05.280782Z"
}
```

当前行为：

- 接口要求当前请求携带有效登录会话。
- 接口要求当前登录用户在 `admin_accounts` 中存在激活态管理员记录。
- 成功后会：
  - 更新目标用户 `auth_level = campus_app_verified`
  - 写入或更新 `user_auth_identities` 中的 `campus_app` 绑定
  - 写入 `audit_logs`

当前错误码：

- `VALIDATION_FAILED`
- `AUTH_LOGIN_REQUIRED`
- `ADMIN_FORBIDDEN`
- `USER_NOT_FOUND`

## 2. 当前实现说明

- 当前 `admin` 模块已经落地第一条可执行后台能力：手工标记校园认证。
- `admin` 持久层和 `auth` 一样，运行环境满足条件时走 MyBatis；测试/本地无数据库依赖时回退到内存实现。
- 当前管理员权限判断只校验 `admin_accounts.status = active`，后续再细化 `admin_role` 权限矩阵。
