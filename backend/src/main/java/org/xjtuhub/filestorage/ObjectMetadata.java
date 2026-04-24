package org.xjtuhub.filestorage;

import java.time.Instant;

public record ObjectMetadata(
        String objectKey,
        String contentType,
        long sizeBytes,
        String checksum,
        Instant lastModifiedAt
) {
}
