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

## 2. 当前管理员身份查询

```text
GET /api/v1/admin/me
```

用途：

- 返回当前登录用户对应的激活管理员账号与角色信息，供后台入口确认当前管理员身份。

请求体：

- 无。

成功响应 `data`：

```json
{
  "adminAccountId": "1776997864826004",
  "userId": "1776997864824002",
  "adminRole": "super_admin",
  "status": "active"
}
```

当前行为：

- 接口要求当前请求携带有效登录会话。
- 接口要求当前登录用户在 `admin_accounts` 中存在激活状态管理员记录。
- 当前仅返回管理员账号 ID、用户 ID、角色和状态，不在前端推导权限。

当前错误码：

- `AUTH_LOGIN_REQUIRED`
- `ADMIN_FORBIDDEN`

## 3. 管理员操作审计列表

```text
GET /api/v1/admin/audit-logs?page=1&pageSize=20
```

用途：

- 面向后台管理员返回审计日志分页列表，用于查看管理员操作轨迹。
- 当前版本只提供基础分页，不提供复杂过滤。

查询参数：

- `page` 可选，默认 `1`，最小 `1`。
- `pageSize` 可选，默认 `20`，最小 `1`，最大 `50`。

成功响应 `data`：

```json
{
  "items": [
    {
      "id": "1776999451861010",
      "actorUserId": "1776999451800002",
      "adminAccountId": "1776999451838009",
      "action": "admin_mark_campus_verification",
      "targetType": "user",
      "targetId": "1776999451831006",
      "requestId": "req_06a25e01bb1840e08a9d99c232972151",
      "ipHash": "12ca17b49af2289436f303e0166030a21e525d266e209267433801a8fd4071a0",
      "userAgentHash": null,
      "detailsJson": "{\"note\":\"manual verification\",\"previousAuthLevel\":\"email_user\"}",
      "createdAt": "2026-04-24T02:57:31.855688Z"
    }
  ],
  "page": 1,
  "pageSize": 20,
  "total": 1,
  "hasNext": false
}
```

当前行为：

- 接口要求当前请求携带有效登录会话。
- 接口要求当前登录用户在 `admin_accounts` 中存在激活状态管理员记录。
- 列表按 `createdAt`、`id` 倒序返回。
- 响应不暴露原始 `ipAddress`，只返回 `ipHash` 与 `userAgentHash`。

当前错误码：

- `VALIDATION_FAILED`
- `AUTH_LOGIN_REQUIRED`
- `ADMIN_FORBIDDEN`

## 4. 校园认证手工标记历史查询

```text
GET /api/v1/admin/users/{userId}/campus-verification/history?page=1&pageSize=20
```

用途：

- 按目标用户查询手工标记校园认证产生的审计记录。
- 面向后台排障与追踪，便于确认谁在何时对目标用户做过手工认证标记。

路径参数：

- `userId` 必须是数字字符串。

查询参数：

- `page` 可选，默认 `1`，最小 `1`。
- `pageSize` 可选，默认 `20`，最小 `1`，最大 `50`。

成功响应 `data`：

```json
{
  "items": [
    {
      "id": "1776999849093015",
      "actorUserId": "1776999849030002",
      "adminAccountId": "1776999849066013",
      "action": "admin_mark_campus_verification",
      "targetType": "user",
      "targetId": "1776999849060006",
      "requestId": "req_a70e7db4a65b4eacbebf6d0a1b0a997b",
      "ipHash": "12ca17b49af2289436f303e0166030a21e525d266e209267433801a8fd4071a0",
      "userAgentHash": null,
      "detailsJson": "{\"note\":\"second target mark\",\"previousAuthLevel\":\"campus_app_verified\"}",
      "createdAt": "2026-04-24T03:04:09.096697Z"
    }
  ],
  "page": 1,
  "pageSize": 20,
  "total": 2,
  "hasNext": false
}
```

当前行为：

- 接口要求当前请求携带有效登录会话。
- 接口要求当前登录用户在 `admin_accounts` 中存在激活状态管理员记录。
- 只返回 `action = admin_mark_campus_verification`、`targetType = user`、`targetId = userId` 的审计记录。
- 列表按 `createdAt`、`id` 倒序返回。
- 响应不暴露原始 `ipAddress`。

当前错误码：

- `VALIDATION_FAILED`
- `AUTH_LOGIN_REQUIRED`
- `ADMIN_FORBIDDEN`

## 5. 用户身份绑定管理查询

```text
GET /api/v1/admin/users/{userId}/identity-bindings
```

用途：

- 面向后台管理员查询指定用户已经绑定的登录/认证身份。
- 配合后台核验用户身份、排查校园认证状态使用。

路径参数：

- `userId` 必须是数字字符串。

成功响应 `data`：

```json
{
  "userId": "1777010082149006",
  "identityBindings": [
    {
      "provider": "email",
      "providerDisplay": "student@example.com",
      "verificationStatus": "verified",
      "primary": true,
      "lastUsed": true
    },
    {
      "provider": "campus_app",
      "providerDisplay": "校园认证（后台标记）",
      "verificationStatus": "verified",
      "primary": false,
      "lastUsed": false
    }
  ]
}
```

当前行为：

- 接口要求当前请求携带有效登录会话。
- 接口要求当前登录用户在 `admin_accounts` 中存在激活状态管理员记录。
- `primary` 和 `lastUsed` 由后端根据用户记录计算，前端不自行推导。

当前错误码：

- `VALIDATION_FAILED`
- `AUTH_LOGIN_REQUIRED`
- `ADMIN_FORBIDDEN`
- `USER_NOT_FOUND`

## 6. 当前实现说明

- 当前 `admin` 模块已经落地第一条可执行后台能力：手工标记校园认证。
- `admin` 持久层和 `auth` 一样，运行环境满足条件时走 MyBatis；测试/本地无数据库依赖时回退到内存实现。
- 当前管理员权限判断只校验 `admin_accounts.status = active`，后续再细化 `admin_role` 权限矩阵。
