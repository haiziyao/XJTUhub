package org.xjtuhub.admin;

import java.time.Instant;
import java.util.Optional;

interface AdminStore {
    Optional<StoredAdminAccount> findActiveAdminByUserId(long userId);

    Optional<StoredUserRecord> findUserById(long userId);

    void markCampusVerification(long targetUserId, long actorUserId, Instant now);

    void writeAuditLog(
            Long actorUserId,
            Long adminAccountId,
            String action,
            String targetType,
            Long targetId,
            String requestId,
            String ipAddress,
            String ipHash,
            String userAgentHash,
            String detailsJson,
            Instant now
    );

    record StoredAdminAccount(
            long id,
            long userId,
            String adminRole,
            String status
    ) {
    }

    record StoredUserRecord(
            long id,
            String authLevel
    ) {
    }
}
