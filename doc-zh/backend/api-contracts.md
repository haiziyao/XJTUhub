# API 合约

> **权威性说明：** 本文件是 `doc/backend/api-contracts.md` 的中文镜像，只供人工阅读。AI agent 和实现工作必须以英文 `doc/` 为准。

后端 API 是前端唯一受支持的集成点。

## API 归属

- 前端不能直接连接 MySQL、Redis、Elasticsearch、MinIO、阿里 OSS、腾讯 COS 或邮件供应商。
- API 响应必须显式暴露权限、审核、身份和会员状态。
- 前端不能基于本地假设推导特权状态。

## 响应结构

除非有框架层面的文档化理由，否则使用一致的响应封装。

推荐结构：

```json
{
  "data": {},
  "error": null,
  "requestId": "string"
}
```

错误结构：

```json
{
  "data": null,
  "error": {
    "code": "CONTENT_NOT_FOUND",
    "message": "Content not found",
    "details": {}
  },
  "requestId": "string"
}
```

## 分页

列表 API 必须明确分页方式。

推荐字段：

- `items`
- `page`
- `pageSize`
- `total`
- `hasNext`

当 offset 分页不合适时，feed 和时间线可以使用 cursor 分页。

## 版本

- 公共 API 路径应带版本，例如 `/api/v1`。
- 破坏性变更需要文档化迁移。
- 前端页面 spec 必须引用其依赖的 API 版本。

## 认证和授权

- API 必须在后端校验权限。
- 认证失败和授权失败必须使用不同错误码。
- 敏感动作需要审计日志。

## 内容卡片

搜索、板块列表、个人资料列表和相关内容列表应尽可能返回统一内容卡片 DTO：

- id。
- type。
- title。
- excerpt。
- board。
- tags。
- author display。
- anonymous display state。
- membership display state。
- review label。
- reaction summary。
- published time。
