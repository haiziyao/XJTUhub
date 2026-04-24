package org.xjtuhub.filestorage;

import java.time.Instant;

public record PresignedObjectUrl(
        String url,
        String method,
        Instant expiresAt
) {
}
