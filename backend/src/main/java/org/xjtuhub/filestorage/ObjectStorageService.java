package org.xjtuhub.filestorage;

import java.time.Duration;

public interface ObjectStorageService {
    PresignedObjectUrl createUploadUrl(ObjectUploadRequest request);

    void completeUpload(String objectKey);

    PresignedObjectUrl getDownloadUrl(String objectKey, Duration expiresIn);

    void deleteObject(String objectKey);

    void copyObject(String sourceObjectKey, String destinationObjectKey);

    ObjectMetadata getObjectMetadata(String objectKey);
}
