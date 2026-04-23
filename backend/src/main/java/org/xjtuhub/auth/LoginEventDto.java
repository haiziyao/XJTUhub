package org.xjtuhub.auth;

import java.time.Instant;

public record LoginEventDto(
        String id,
        String provider,
        String eventType,
        boolean success,
        String failureReason,
        Instant createdAt
) {
}
