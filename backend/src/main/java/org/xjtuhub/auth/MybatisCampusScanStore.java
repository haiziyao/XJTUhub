package org.xjtuhub.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.xjtuhub.auth.persistence.entity.CampusAppLoginSessionEntity;
import org.xjtuhub.auth.persistence.mapper.CampusAppLoginSessionMapper;
import org.xjtuhub.common.support.IdGenerator;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

class MybatisCampusScanStore implements CampusScanStore {
    private final CampusAppLoginSessionMapper mapper;
    private final IdGenerator idGenerator;

    MybatisCampusScanStore(CampusAppLoginSessionMapper mapper, IdGenerator idGenerator) {
        this.mapper = mapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public StoredCampusScanSession createSession(String sceneId, String qrTokenHash, Instant expiresAt, Instant now) {
        CampusAppLoginSessionEntity entity = new CampusAppLoginSessionEntity();
        entity.setId(idGenerator.nextId());
        entity.setSceneId(sceneId);
        entity.setQrTokenHash(qrTokenHash);
        entity.setStatus("pending");
        entity.setExpiresAt(ts(expiresAt));
        entity.setCreatedAt(ts(now));
        entity.setUpdatedAt(ts(now));
        mapper.insert(entity);
        return toStored(entity);
    }

    @Override
    public Optional<StoredCampusScanSession> findBySceneId(String sceneId) {
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<CampusAppLoginSessionEntity>()
                        .eq(CampusAppLoginSessionEntity::getSceneId, sceneId)
                        .last("LIMIT 1")
        )).map(this::toStored);
    }

    @Override
    public Optional<StoredCampusScanSession> findByQrTokenHash(String qrTokenHash) {
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<CampusAppLoginSessionEntity>()
                        .eq(CampusAppLoginSessionEntity::getQrTokenHash, qrTokenHash)
                        .last("LIMIT 1")
        )).map(this::toStored);
    }

    @Override
    public void markScanned(String sceneId, long matchedUserId, Instant now) {
        mapper.update(
                null,
                new LambdaUpdateWrapper<CampusAppLoginSessionEntity>()
                        .eq(CampusAppLoginSessionEntity::getSceneId, sceneId)
                        .set(CampusAppLoginSessionEntity::getStatus, "scanned")
                        .set(CampusAppLoginSessionEntity::getMatchedUserId, matchedUserId)
                        .set(CampusAppLoginSessionEntity::getScannedAt, ts(now))
                        .set(CampusAppLoginSessionEntity::getUpdatedAt, ts(now))
        );
    }

    @Override
    public void markConfirmed(String sceneId, Instant now) {
        mapper.update(
                null,
                new LambdaUpdateWrapper<CampusAppLoginSessionEntity>()
                        .eq(CampusAppLoginSessionEntity::getSceneId, sceneId)
                        .set(CampusAppLoginSessionEntity::getStatus, "confirmed")
                        .set(CampusAppLoginSessionEntity::getConfirmedAt, ts(now))
                        .set(CampusAppLoginSessionEntity::getUpdatedAt, ts(now))
        );
    }

    @Override
    public void markCanceled(String sceneId, Instant now) {
        mapper.update(
                null,
                new LambdaUpdateWrapper<CampusAppLoginSessionEntity>()
                        .eq(CampusAppLoginSessionEntity::getSceneId, sceneId)
                        .set(CampusAppLoginSessionEntity::getStatus, "canceled")
                        .set(CampusAppLoginSessionEntity::getCanceledAt, ts(now))
                        .set(CampusAppLoginSessionEntity::getUpdatedAt, ts(now))
        );
    }

    private StoredCampusScanSession toStored(CampusAppLoginSessionEntity entity) {
        return new StoredCampusScanSession(
                entity.getId(),
                entity.getSceneId(),
                entity.getQrTokenHash(),
                entity.getStatus(),
                entity.getMatchedUserId(),
                instant(entity.getExpiresAt()),
                instant(entity.getScannedAt()),
                instant(entity.getConfirmedAt()),
                instant(entity.getCanceledAt()),
                instant(entity.getCreatedAt()),
                instant(entity.getUpdatedAt())
        );
    }

    private Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
