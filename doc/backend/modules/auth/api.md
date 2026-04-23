# Auth 模块接口文档

本文档描述当前已经实现的 `auth` 模块接口。

基础路径：

```text
/api/v1/auth
```

全局响应包络、统一错误结构、请求 ID、耗时字段继承自：

- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/prd/global-auth-organization-permission.md`

## 1. 创建邮箱验证码

```text
POST /api/v1/auth/email-tokens
```

用途：

- 为邮箱登录创建一次性验证码。

请求：

```json
{
  "email": "student@example.com",
  "purpose": "login"
}
```

当前校验：

- `email` 必须是合法邮箱地址。
- `purpose` 不能为空。

成功响应 `data`：

```json
{
  "email": "student@example.com",
  "purpose": "login",
  "expiresAt": "2026-04-23T17:24:54.131Z",
  "delivery": "accepted",
  "token": null
}
```

行为说明：

- 当 `xjtuhub.auth.email.debug-return-token=true` 时，`delivery` 为 `debug_return`，并直接返回明文验证码。
- 默认通过 `EmailSender` 发送验证码，接口不返回明文验证码。
- 当前按 `email + purpose` 做创建限流。

当前错误码：

- `VALIDATION_FAILED`
- `RATE_LIMITED`

## 2. 创建邮箱登录会话

```text
POST /api/v1/auth/email-sessions
```

用途：

- 校验邮箱验证码。
- 创建 HttpOnly 会话 Cookie。

请求：

```json
{
  "email": "student@example.com",
  "purpose": "login",
  "token": "raw-token",
  "deviceLabel": "Chrome on Windows"
}
```

成功响应 `data`：

```json
{
  "user": {
    "id": "1776964323268002",
    "nickname": "student",
    "avatarUrl": null,
    "bio": null,
    "authLevel": "email_user",
    "nameColor": "default",
    "primaryIdentityProvider": "email",
    "lastLoginProvider": "email",
    "identitySummary": "邮箱已验证",
    "identityBindings": [
      {
        "provider": "email",
        "providerDisplay": "student@example.com",
        "verificationStatus": "verified",
        "primary": true,
        "lastUsed": true
      }
    ],
    "displayBadges": [
      {
        "type": "identity",
        "code": "email_verified",
        "label": "Email Verified",
        "tone": "neutral"
      }
    ]
  },
  "session": {
    "id": "1776964323376007",
    "loginProvider": "email",
    "deviceLabel": "Chrome on Windows",
    "createdAt": "2026-04-23T17:12:03.376Z",
    "expiresAt": "2026-05-23T17:12:03.376Z",
    "lastSeenAt": "2026-04-23T17:12:03.376Z",
    "current": true
  }
}
```

Cookie 行为：

- 设置 `XJTUHUB_SESSION`。
- Cookie 为 `HttpOnly`。
- Cookie 路径为 `/`。
- Cookie 使用 `SameSite=Lax`。

限流说明：

- 当前对验证码校验失败次数做限流。
- 限流键为 `email + purpose`。
- 当前默认实现为应用内存存储，后续可以替换为 Redis 实现而不改接口。

当前错误码：

- `VALIDATION_FAILED`
- `RATE_LIMITED`
- `AUTH_EMAIL_TOKEN_INVALID`
- `AUTH_EMAIL_TOKEN_EXPIRED`
- `AUTH_EMAIL_TOKEN_CONSUMED`

## 3. 校园扫码登录保留接口

以下接口已保留，但当前阶段不实现真实校园协议：

```text
POST /api/v1/auth/campus-scan/sessions
GET  /api/v1/auth/campus-scan/sessions/{sceneId}
POST /api/v1/auth/campus-scan/sessions/{sceneId}/confirm
```

当前行为：

- 统一返回 `501 Not Implemented`。
- 统一错误码为 `AUTH_CAMPUS_SCAN_RESERVED`。
- 该返回表示接口边界已经预留，后续接入真实校园官方 APP 协议时沿用此路由前缀。

## 4. 获取当前用户会话列表

```text
GET /api/v1/auth/sessions
```

用途：

- 返回当前用户的活跃会话列表。

成功响应 `data`：

```json
{
  "items": [
    {
      "id": "1776964323376007",
      "loginProvider": "email",
      "deviceLabel": "Chrome on Windows",
      "createdAt": "2026-04-23T17:12:03.376Z",
      "expiresAt": "2026-05-23T17:12:03.376Z",
      "lastSeenAt": "2026-04-23T17:13:21.011Z",
      "current": true
    }
  ],
  "page": 1,
  "pageSize": 1,
  "total": 1,
  "hasNext": false
}
```

行为说明：

- 当前会话在通过鉴权后会刷新 `lastSeenAt`。

当前错误码：

- `AUTH_LOGIN_REQUIRED`

## 5. 注销当前会话

```text
DELETE /api/v1/auth/sessions/current
```

用途：

- 注销当前会话并清除当前会话 Cookie。

成功响应 `data`：

```json
{
  "revoked": true
}
```

当前错误码：

- `AUTH_LOGIN_REQUIRED`

## 6. 按会话 ID 注销会话

```text
DELETE /api/v1/auth/sessions/{sessionId}
```

用途：

- 注销当前用户名下的指定活跃会话。

规则：

- `sessionId` 必须是数字字符串。
- 目标会话必须属于当前用户。

成功响应 `data`：

```json
{
  "revoked": true
}
```

当前错误码：

- `VALIDATION_FAILED`
- `AUTH_LOGIN_REQUIRED`
- `AUTH_FORBIDDEN`
- `AUTH_SESSION_EXPIRED`

## 7. 获取登录历史

```text
GET /api/v1/auth/login-events
```

用途：

- 返回当前用户的安全版登录历史视图。

成功响应 `data`：

```json
{
  "items": [
    {
      "id": "1776964564241013",
      "provider": "email",
      "eventType": "email_token_login",
      "success": true,
      "failureReason": null,
      "createdAt": "2026-04-23T17:16:04.241Z"
    }
  ],
  "page": 1,
  "pageSize": 1,
  "total": 1,
  "hasNext": false
}
```

安全说明：

- 普通接口不暴露 `ipAddress`。
- 普通接口不暴露 `ipHash`。
- 普通接口不暴露 `userAgentHash`。

当前错误码：

- `AUTH_LOGIN_REQUIRED`

## 8. 当前实现说明

- 运行环境存在数据库和 MyBatis 会话工厂时，持久层使用 `MybatisAuthStore`。
- 测试环境默认回退到 `InMemoryAuthStore`。
- 邮件发送通过 `EmailSender` 抽象，默认实现为 `LoggingEmailSender`。
- 数据访问层已经切换到 MyBatis / MyBatis-Plus。
- 会话最近活跃时间通过 `sessions.last_seen_at` 维护。
- 登录会话创建时会写入 `sessions.ip_address`、`sessions.ip_hash`、`sessions.user_agent_hash`。
- 验证码校验限流优先使用 Redis；当前运行环境没有 `StringRedisTemplate` 时回退到内存实现。
- 当前用户接口会返回后端计算的 `identitySummary` 和 `identityBindings`，供前端在昵称后展示身份来源与绑定情况。
- 校园扫码登录目前只预留接口边界，不实现任何伪生产协议逻辑。
