# Storage And Files

Object storage must be accessed through an internal abstraction.

## Storage Providers

Phase 1 provider:

- MinIO.

Future providers:

- Aliyun OSS.
- Tencent COS.

## Required Interface

Business modules should depend on an interface like:

```text
ObjectStorageService
- createUploadUrl(...)
- completeUpload(...)
- getDownloadUrl(...)
- deleteObject(...)
- copyObject(...)
- getObjectMetadata(...)
```

Provider-specific code belongs in adapters:

- `MinIOObjectStorageAdapter`
- `AliyunOssStorageAdapter`
- `TencentCosStorageAdapter`

Current implementation:

- `ObjectStorageService`
- `MinioObjectStorageAdapter`
- `InMemoryObjectStorageService` for tests and local fallback when MinIO is not configured.

See also: `doc/backend/modules/file_storage/api.md`.

## Attachment Metadata

Do not store provider-only concepts as the only attachment identity.

Required logical fields:

- id.
- content id.
- storage provider.
- bucket.
- object key.
- file name.
- MIME type.
- size bytes.
- checksum.
- visibility.
- review status.
- file status.
- created time.

## Download Rules

- Uploaded resources may be downloadable before review.
- Unreviewed downloads must clearly expose unreviewed state to the frontend.
- Rejected or disputed downloads are controlled by backend policy.
- Download APIs should record logs needed for abuse investigation and resource statistics.

## Migration Rules

Provider migration must not require changes to content, comment, review, or notification modules. Migration should be handled by storage adapters, data migration scripts, and attachment metadata updates.
