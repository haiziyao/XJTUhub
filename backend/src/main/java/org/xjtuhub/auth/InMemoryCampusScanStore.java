package org.xjtuhub.auth;

import org.springframework.stereotype.Component;
import org.xjtuhub.common.support.IdGenerator;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
class InMemoryCampusScanStore implements CampusScanStore {
    private final IdGenerator idGenerator;
    private final Map<String, StoredCampusScanSession> sessionsBySceneId = new ConcurrentHashMap<>();
    private final Map<String, String> sceneIdsByQrTokenHash = new ConcurrentHashMap<>();

    InMemoryCampusScanStore(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public StoredCampusScanSession createSession(String sceneId, String qrTokenHash, Instant expiresAt, Instant now) {
        StoredCampusScanSession session = new StoredCampusScanSession(
                idGenerator.nextId(),
                sceneId,
                qrTokenHash,
                "pending",
                null,
                expiresAt,
                null,
                null,
                null,
                now,
                now
        );
        sessionsBySceneId.put(sceneId, session);
        sceneIdsByQrTokenHash.put(qrTokenHash, sceneId);
        return session;
    }

    @Override
    public Optional<StoredCampusScanSession> findBySceneId(String sceneId) {
        return Optional.ofNullable(sessionsBySceneId.get(sceneId));
    }

    @Override
    public Optional<StoredCampusScanSession> findByQrTokenHash(String qrTokenHash) {
        String sceneId = sceneIdsByQrTokenHash.get(qrTokenHash);
        return sceneId == null ? Optional.empty() : findBySceneId(sceneId);
    }

    @Override
    public void markScanned(String sceneId, long matchedUserId, Instant now) {
        update(sceneId, session -> new StoredCampusScanSession(
                session.id(),
                session.sceneId(),
                session.qrTokenHash(),
                "scanned",
                matchedUserId,
                session.expiresAt(),
                now,
                session.confirmedAt(),
                session.canceledAt(),
                session.createdAt(),
                now
        ));
    }

    @Override
    public void markConfirmed(String sceneId, Instant now) {
        update(sceneId, session -> new StoredCampusScanSession(
                session.id(),
                session.sceneId(),
                session.qrTokenHash(),
                "confirmed",
                session.matchedUserId(),
                session.expiresAt(),
                session.scannedAt(),
                now,
                session.canceledAt(),
                session.createdAt(),
                now
        ));
    }

    @Override
    public void markCanceled(String sceneId, Instant now) {
        update(sceneId, session -> new StoredCampusScanSession(
                session.id(),
                session.sceneId(),
                session.qrTokenHash(),
                "canceled",
                session.matchedUserId(),
                session.expiresAt(),
                session.scannedAt(),
                session.confirmedAt(),
                now,
                session.createdAt(),
                now
        ));
    }

    private void update(String sceneId, java.util.function.Function<StoredCampusScanSession, StoredCampusScanSession> updater) {
        sessionsBySceneId.computeIfPresent(sceneId, (key, session) -> updater.apply(session));
    }
}
