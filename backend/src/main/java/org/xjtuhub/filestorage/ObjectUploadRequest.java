package org.xjtuhub.filestorage;

import java.time.Duration;

public record ObjectUploadRequest(
        String objectKey,
        String contentType,
        long sizeBytes,
        Duration expiresIn
) {
}
