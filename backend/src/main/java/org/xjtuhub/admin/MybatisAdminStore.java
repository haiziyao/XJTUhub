package org.xjtuhub.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.xjtuhub.admin.persistence.entity.AdminAccountEntity;
import org.xjtuhub.admin.persistence.entity.AuditLogEntity;
import org.xjtuhub.admin.persistence.mapper.AdminAccountMapper;
import org.xjtuhub.admin.persistence.mapper.AuditLogMapper;
import org.xjtuhub.auth.persistence.entity.UserAuthIdentityEntity;
import org.xjtuhub.auth.persistence.entity.UserEntity;
import org.xjtuhub.auth.persistence.mapper.UserAuthIdentityMapper;
import org.xjtuhub.auth.persistence.mapper.UserMapper;
import org.xjtuhub.common.support.IdGenerator;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

class MybatisAdminStore implements AdminStore {
    private final AdminAccountMapper adminAccountMapper;
    private final AuditLogMapper auditLogMapper;
    private final UserMapper userMapper;
    private final UserAuthIdentityMapper userAuthIdentityMapper;
    private final IdGenerator idGenerator;

    MybatisAdminStore(
            AdminAccountMapper adminAccountMapper,
            AuditLogMapper auditLogMapper,
            UserMapper userMapper,
            UserAuthIdentityMapper userAuthIdentityMapper,
            IdGenerator idGenerator
    ) {
        this.adminAccountMapper = adminAccountMapper;
        this.auditLogMapper = auditLogMapper;
        this.userMapper = userMapper;
        this.userAuthIdentityMapper = userAuthIdentityMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public Optional<StoredAdminAccount> findActiveAdminByUserId(long userId) {
        AdminAccountEntity entity = adminAccountMapper.selectOne(
                new LambdaQueryWrapper<AdminAccountEntity>()
                        .eq(AdminAccountEntity::getUserId, userId)
                        .eq(AdminAccountEntity::getStatus, "active")
                        .last("LIMIT 1")
        );
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new StoredAdminAccount(entity.getId(), entity.getUserId(), entity.getAdminRole(), entity.getStatus()));
    }

    @Override
    public Optional<StoredUserRecord> findUserById(long userId) {
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null || entity.getDeletedAt() != null) {
            return Optional.empty();
        }
        return Optional.of(new StoredUserRecord(entity.getId(), entity.getAuthLevel()));
    }

    @Override
    public void markCampusVerification(long targetUserId, long actorUserId, Instant now) {
        userMapper.update(
                null,
                new LambdaUpdateWrapper<UserEntity>()
                        .eq(UserEntity::getId, targetUserId)
                        .set(UserEntity::getAuthLevel, "campus_app_verified")
                        .set(UserEntity::getUpdatedAt, ts(now))
        );

        UserAuthIdentityEntity identity = userAuthIdentityMapper.selectOne(
                new LambdaQueryWrapper<UserAuthIdentityEntity>()
                        .eq(UserAuthIdentityEntity::getUserId, targetUserId)
                        .eq(UserAuthIdentityEntity::getProvider, "campus_app")
                        .isNull(UserAuthIdentityEntity::getDeletedAt)
                        .last("LIMIT 1")
        );
        if (identity == null) {
            identity = new UserAuthIdentityEntity();
            identity.setId(idGenerator.nextId());
            identity.setUserId(targetUserId);
            identity.setProvider("campus_app");
            identity.setProviderSubject("manual:" + targetUserId);
            identity.setProviderDisplay("校园认证（后台标记）");
            identity.setVerificationStatus("verified");
            identity.setVerifiedAt(ts(now));
            identity.setLastUsedAt(ts(now));
            identity.setCreatedAt(ts(now));
            identity.setUpdatedAt(ts(now));
            userAuthIdentityMapper.insert(identity);
            return;
        }

        userAuthIdentityMapper.update(
                null,
                new LambdaUpdateWrapper<UserAuthIdentityEntity>()
                        .eq(UserAuthIdentityEntity::getId, identity.getId())
                        .set(UserAuthIdentityEntity::getProviderDisplay, "校园认证（后台标记）")
                        .set(UserAuthIdentityEntity::getVerificationStatus, "verified")
                        .set(UserAuthIdentityEntity::getVerifiedAt, ts(now))
                        .set(UserAuthIdentityEntity::getLastUsedAt, ts(now))
                        .set(UserAuthIdentityEntity::getUpdatedAt, ts(now))
        );
    }

    @Override
    public List<StoredAuditLog> listAuditLogs(int offset, int limit) {
        return auditLogMapper.selectList(
                new LambdaQueryWrapper<AuditLogEntity>()
                        .orderByDesc(AuditLogEntity::getCreatedAt)
                        .orderByDesc(AuditLogEntity::getId)
                        .last("LIMIT " + limit + " OFFSET " + offset)
        ).stream()
                .map(this::toStoredAuditLog)
                .toList();
    }

    @Override
    public long countAuditLogs() {
        return auditLogMapper.selectCount(null);
    }

    @Override
    public List<StoredAuditLog> listAuditLogsByActionAndTarget(String action, String targetType, long targetId, int offset, int limit) {
        return auditLogMapper.selectList(
                new LambdaQueryWrapper<AuditLogEntity>()
                        .eq(AuditLogEntity::getAction, action)
                        .eq(AuditLogEntity::getTargetType, targetType)
                        .eq(AuditLogEntity::getTargetId, targetId)
                        .orderByDesc(AuditLogEntity::getCreatedAt)
                        .orderByDesc(AuditLogEntity::getId)
                        .last("LIMIT " + limit + " OFFSET " + offset)
        ).stream()
                .map(this::toStoredAuditLog)
                .toList();
    }

    @Override
    public long countAuditLogsByActionAndTarget(String action, String targetType, long targetId) {
        return auditLogMapper.selectCount(
                new LambdaQueryWrapper<AuditLogEntity>()
                        .eq(AuditLogEntity::getAction, action)
                        .eq(AuditLogEntity::getTargetType, targetType)
                        .eq(AuditLogEntity::getTargetId, targetId)
        );
    }

    @Override
    public void writeAuditLog(Long actorUserId, Long adminAccountId, String action, String targetType, Long targetId, String requestId, String ipAddress, String ipHash, String userAgentHash, String detailsJson, Instant now) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(idGenerator.nextId());
        entity.setActorUserId(actorUserId);
        entity.setAdminAccountId(adminAccountId);
        entity.setAction(action);
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setRequestId(requestId);
        entity.setIpAddress(ipAddress);
        entity.setIpHash(ipHash);
        entity.setUserAgentHash(userAgentHash);
        entity.setDetailsJson(detailsJson);
        entity.setCreatedAt(ts(now));
        auditLogMapper.insert(entity);
    }

    private StoredAuditLog toStoredAuditLog(AuditLogEntity entity) {
        return new StoredAuditLog(
                entity.getId(),
                entity.getActorUserId(),
                entity.getAdminAccountId(),
                entity.getAction(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getRequestId(),
                entity.getIpHash(),
                entity.getUserAgentHash(),
                entity.getDetailsJson(),
                entity.getCreatedAt().toInstant()
        );
    }

    private Timestamp ts(Instant instant) {
        return Timestamp.from(instant);
    }
}
