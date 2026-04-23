# User 模块接口文档

本文档描述当前已经实现的 `user` 模块接口。

基础路径：

```text
/api/v1/users
```

全局响应包络、统一错误结构、DTO 约束继承自：

- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/prd/global-auth-organization-permission.md`

## 1. 获取当前用户

```text
GET /api/v1/users/me
```

用途：

- 返回当前已登录用户的基础资料摘要。

成功响应 `data`：

```json
{
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
}
```

行为说明：

- 用户存在有效大会员时，`nameColor` 为 `red`。
- `displayBadges` 由后端计算。
- `identitySummary` 由后端计算，可直接用于昵称后的身份展示文案。
- `identityBindings` 返回当前账号已绑定的登录身份列表，供前端渲染身份标记。

当前错误码：

- `AUTH_LOGIN_REQUIRED`
- `USER_NOT_FOUND`

## 2. 更新当前用户资料

```text
PATCH /api/v1/users/me
```

用途：

- 更新当前用户可编辑的资料字段。

请求：

```json
{
  "nickname": "Profile User",
  "bio": "Updated profile bio",
  "avatarUrl": "https://example.com/avatar.png"
}
```

当前校验：

- `nickname` 必填。
- `nickname` 最大长度为 `64`。
- `nickname` 不能是全空白字符。
- `bio` 最大长度为 `512`。
- `avatarUrl` 最大长度为 `512`。
- `avatarUrl` 如果传值，必须是合法的 `http` 或 `https` URL。

成功响应：

- 返回与 `GET /api/v1/users/me` 相同的 DTO 结构。

字段处理：

- `nickname` 会在写入前去掉首尾空白。
- `bio` 为空白时会被写为 `null`。
- `avatarUrl` 为空白时会被写为 `null`。

当前错误码：

- `VALIDATION_FAILED`
- `AUTH_LOGIN_REQUIRED`
- `USER_NOT_FOUND`

## 3. 当前实现说明

- 资料更新当前直接写入 `users.nickname`、`users.bio`、`users.avatar_url`。
- 目前还没有独立头像上传流程，`avatarUrl` 暂时只是普通 URL 字段。
- 后续新增用户资料能力时，继续在本文档追加。
