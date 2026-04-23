# User 模块接口文档

本文档描述当前已经实现的 `user` 模块接口。

基础路径：

```text
/api/v1/users
```

全局响应包络、错误处理与 DTO 约束继承自：

- `doc/backend/prd/global-api-contracts.md`
- `doc/backend/prd/global-auth-organization-permission.md`

## 1. 获取当前用户

```text
GET /api/v1/users/me
```

用途：

- 返回当前已登录用户的基础资料摘要，供普通客户端使用。

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

- 当用户存在有效大会员时，`nameColor` 为 `red`。
- `displayBadges` 由后端计算。

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
- `bio` 最大长度为 `512`。
- `avatarUrl` 最大长度为 `512`。

成功响应：

- 返回与 `GET /api/v1/users/me` 相同的 DTO 结构。

当前错误码：

- `VALIDATION_FAILED`
- `AUTH_LOGIN_REQUIRED`
- `USER_NOT_FOUND`

## 3. 当前实现说明

- 资料更新当前直接写入 `users.nickname`、`users.bio`、`users.avatar_url`。
- 目前还没有独立头像上传流程，`avatarUrl` 暂时只是普通 URL 字段。
- 后续 user 模块新增能力时，继续在本文件追加文档。
