# 存储和文件

> **权威性说明：** 本文件是 `doc/backend/storage-and-files.md` 的中文镜像，只供人工阅读。AI agent 和实现工作必须以英文 `doc/` 为准。

对象存储必须通过内部抽象访问。

## 存储供应商

Phase 1 供应商：

- MinIO。

未来供应商：

- 阿里 OSS。
- 腾讯 COS。

## 必需接口

业务模块应依赖类似下面的接口：

```text
ObjectStorageService
- createUploadUrl(...)
- completeUpload(...)
- getDownloadUrl(...)
- deleteObject(...)
- copyObject(...)
- getObjectMetadata(...)
```

供应商专属代码属于适配器：

- `MinIOObjectStorageAdapter`
- `AliyunOssStorageAdapter`
- `TencentCosStorageAdapter`

## 附件元数据

不要把供应商专属概念作为附件的唯一身份。

必需逻辑字段：

- id。
- content id。
- storage provider。
- bucket。
- object key。
- file name。
- MIME type。
- size bytes。
- checksum。
- visibility。
- review status。
- file status。
- created time。

## 下载规则

- 上传资源可以在审核前下载。
- 未审核下载必须向前端清楚暴露未审核状态。
- 被拒绝或有争议的下载由后端策略控制。
- 下载 API 应记录滥用调查和资源统计所需日志。

## 迁移规则

供应商迁移不应要求修改 content、comment、review 或 notification 模块。迁移应由存储适配器、数据迁移脚本和附件元数据更新处理。
