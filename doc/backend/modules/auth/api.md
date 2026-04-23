# Auth 模块接口文档

本文档描述当前已经实现的 `auth` 模块接口。

基础路径：

```text
/api/v1/auth
```

全局响应包络、错误处理、ID 规则与分页规则继承自：

- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/prd/global-auth-organization-permission.md`

## 1. 创建邮箱验证码

```text
POST /api/v1/auth/email-tokens
```

用途：

- 为登录创建邮箱验证码。

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

- 当 `xjtuhub.auth.email.debug-return-token=true` 时，`delivery` 为 `debug_return`，同时 `token` 返回明文验证码。
- 否则模块会调用 `EmailSender`，接口不返回明文验证码。
- 当前按 `email + purpose` 做创建限流。

当前错误码：

- `VALIDATION_FAILED`
- `RATE_LIMITED`

## 2. 创建邮箱登录会话

```text
POST /api/v1/auth/email-sessions
```

用途：

- 消费邮箱验证码并创建 HttpOnly 会话 Cookie。

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
    "current": true
  }
}
```

Cookie 行为：

- 设置 `XJTUHUB_SESSION`。
- Cookie 为 `HttpOnly`。
- Cookie 路径为 `/`。
- Cookie 使用 `SameSite=Lax`。

当前错误码：

- `VALIDATION_FAILED`
- `AUTH_EMAIL_TOKEN_INVALID`
- `AUTH_EMAIL_TOKEN_EXPIRED`
- `AUTH_EMAIL_TOKEN_CONSUMED`

## 3. 获取当前用户会话列表

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
      "current": true
    }
  ],
  "page": 1,
  "pageSize": 1,
  "total": 1,
  "hasNext": false
}
```

当前错误码：

- `AUTH_LOGIN_REQUIRED`

## 4. 注销当前会话

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

## 5. 按会话 ID 注销会话

```text
DELETE /api/v1/auth/sessions/{sessionId}
```

用途：

- 注销当前用户名下的其他活跃会话。

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

## 6. 获取登录历史

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

## 7. 当前实现说明

- 当运行环境存在 `JdbcTemplate` 时，存储层使用 `JdbcAuthStore`。
- 测试环境回退到 `InMemoryAuthStore`。
- 邮件发送通过 `EmailSender` 抽象。
- 默认发送实现为 `LoggingEmailSender`。
- 当前限流通过数据库 / 内存存储统计最近时间窗口内的创建次数实现。
