package org.xjtuhub.filestorage;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
class InMemoryObjectStorageService implements ObjectStorageService {
    private final Map<String, ObjectMetadata> objects = new ConcurrentHashMap<>();

    @Override
    public PresignedObjectUrl createUploadUrl(ObjectUploadRequest request) {
        objects.put(request.objectKey(), new ObjectMetadata(
                request.objectKey(),
                request.contentType(),
                request.sizeBytes(),
                null,
                Instant.now()
        ));
        return new PresignedObjectUrl(
                "/objects/" + request.objectKey(),
                "PUT",
                Instant.now().plus(request.expiresIn())
        );
    }

    @Override
    public void completeUpload(String objectKey) {
        objects.computeIfPresent(objectKey, (key, metadata) -> new ObjectMetadata(
                metadata.objectKey(),
                metadata.contentType(),
                metadata.sizeBytes(),
                metadata.checksum(),
                Instant.now()
        ));
    }

    @Override
    public PresignedObjectUrl getDownloadUrl(String objectKey, Duration expiresIn) {
        return new PresignedObjectUrl(
                "/objects/" + objectKey,
                "GET",
                Instant.now().plus(expiresIn)
        );
    }

    @Override
    public void deleteObject(String objectKey) {
        objects.remove(objectKey);
    }

    @Override
    public void copyObject(String sourceObjectKey, String destinationObjectKey) {
        ObjectMetadata source = getObjectMetadata(sourceObjectKey);
        objects.put(destinationObjectKey, new ObjectMetadata(
                destinationObjectKey,
                source.contentType(),
                source.sizeBytes(),
                source.checksum(),
                Instant.now()
        ));
    }

    @Override
    public ObjectMetadata getObjectMetadata(String objectKey) {
        ObjectMetadata metadata = objects.get(objectKey);
        if (metadata == null) {
            throw new IllegalArgumentException("Object not found: " + objectKey);
        }
        return metadata;
    }
}
