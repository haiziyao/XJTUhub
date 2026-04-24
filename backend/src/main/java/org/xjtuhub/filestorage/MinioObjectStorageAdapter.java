package org.xjtuhub.filestorage;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import org.xjtuhub.system.MinioProperties;

import java.time.Duration;
import java.time.Instant;

class MinioObjectStorageAdapter implements ObjectStorageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    MinioObjectStorageAdapter(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @Override
    public PresignedObjectUrl createUploadUrl(ObjectUploadRequest request) {
        return presignedUrl(request.objectKey(), Method.PUT, request.expiresIn());
    }

    @Override
    public void completeUpload(String objectKey) {
        getObjectMetadata(objectKey);
    }

    @Override
    public PresignedObjectUrl getDownloadUrl(String objectKey, Duration expiresIn) {
        return presignedUrl(objectKey, Method.GET, expiresIn);
    }

    @Override
    public void deleteObject(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName())
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to delete object from MinIO.", ex);
        }
    }

    @Override
    public void copyObject(String sourceObjectKey, String destinationObjectKey) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName())
                    .object(destinationObjectKey)
                    .source(CopySource.builder()
                            .bucket(bucketName())
                            .object(sourceObjectKey)
                            .build())
                    .build());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to copy object in MinIO.", ex);
        }
    }

    @Override
    public ObjectMetadata getObjectMetadata(String objectKey) {
        try {
            StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName())
                    .object(objectKey)
                    .build());
            return new ObjectMetadata(
                    objectKey,
                    response.contentType(),
                    response.size(),
                    response.etag(),
                    response.lastModified() == null ? null : response.lastModified().toInstant()
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read object metadata from MinIO.", ex);
        }
    }

    private PresignedObjectUrl presignedUrl(String objectKey, Method method, Duration expiresIn) {
        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(method)
                    .bucket(bucketName())
                    .object(objectKey)
                    .expiry(Math.toIntExact(expiresIn.toSeconds()))
                    .build());
            return new PresignedObjectUrl(url, method.name(), Instant.now().plus(expiresIn));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create MinIO presigned URL.", ex);
        }
    }

    private String bucketName() {
        return minioProperties.bucketName();
    }
}
