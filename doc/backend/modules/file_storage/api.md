# File Storage 模块接口文档

本文档描述当前已经实现的 `file_storage` 模块能力。

当前阶段尚未开放 HTTP 文件上传/下载接口；已先落地后端内部对象存储统一接口，供后续资源、帖子、评论附件等业务模块复用。

## 1. 对象存储统一接口 MVP

内部接口：

```text
ObjectStorageService
```

当前方法：

- `createUploadUrl(ObjectUploadRequest request)`
- `completeUpload(String objectKey)`
- `getDownloadUrl(String objectKey, Duration expiresIn)`
- `deleteObject(String objectKey)`
- `copyObject(String sourceObjectKey, String destinationObjectKey)`
- `getObjectMetadata(String objectKey)`

请求 DTO：

```json
{
  "objectKey": "resources/intro.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 1024,
  "expiresIn": "PT10M"
}
```

预签名 URL DTO：

```json
{
  "url": "http://minio.example/bucket/resources/intro.pdf?...",
  "method": "PUT",
  "expiresAt": "2026-04-24T06:10:00Z"
}
```

对象元数据 DTO：

```json
{
  "objectKey": "resources/intro.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 1024,
  "checksum": "etag-value",
  "lastModifiedAt": "2026-04-24T06:00:00Z"
}
```

当前实现：

- 存在 `MinioClient` 时使用 `MinioObjectStorageAdapter`。
- 无 MinIO 客户端时回退到 `InMemoryObjectStorageService`，用于测试和本地无对象存储依赖场景。
- 业务模块必须依赖 `ObjectStorageService`，不得直接调用 MinIO。
- 后续接入 Aliyun OSS / Tencent COS 时应新增 adapter，不改变业务模块调用方式。

当前限制：

- 尚未实现附件元数据表读写。
- 尚未实现 HTTP 上传、下载、删除接口。
- 尚未实现对象审核状态、下载审计和资源统计。
