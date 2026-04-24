package org.xjtuhub.auth;

import java.time.Instant;
import java.util.Optional;

interface CampusScanStore {
    StoredCampusScanSession createSession(String sceneId, String qrTokenHash, Instant expiresAt, Instant now);

    Optional<StoredCampusScanSession> findBySceneId(String sceneId);

    Optional<StoredCampusScanSession> findByQrTokenHash(String qrTokenHash);

    void markScanned(String sceneId, long matchedUserId, Instant now);

    void markConfirmed(String sceneId, Instant now);

    void markCanceled(String sceneId, Instant now);

    record StoredCampusScanSession(
            long id,
            String sceneId,
            String qrTokenHash,
            String status,
            Long matchedUserId,
            Instant expiresAt,
            Instant scannedAt,
            Instant confirmedAt,
            Instant canceledAt,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
