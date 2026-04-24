package org.xjtuhub.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import org.xjtuhub.auth.persistence.entity.EmailVerificationTokenEntity;
import org.xjtuhub.auth.persistence.entity.SessionEntity;
import org.xjtuhub.auth.persistence.entity.UserAuthIdentityEntity;
import org.xjtuhub.auth.persistence.entity.UserEntity;
import org.xjtuhub.auth.persistence.entity.UserLoginEventEntity;
import org.xjtuhub.auth.persistence.entity.UserMembershipEntity;
import org.xjtuhub.auth.persistence.mapper.EmailVerificationTokenMapper;
import org.xjtuhub.auth.persistence.mapper.SessionMapper;
import org.xjtuhub.auth.persistence.mapper.UserAuthIdentityMapper;
import org.xjtuhub.auth.persistence.mapper.UserLoginEventMapper;
import org.xjtuhub.auth.persistence.mapper.UserMapper;
import org.xjtuhub.auth.persistence.mapper.UserMembershipMapper;
import org.xjtuhub.common.support.IdGenerator;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnBean(SqlSessionFactory.class)
class MybatisAuthStore implements AuthStore {
    private final EmailVerificationTokenMapper tokenMapper;
    private final UserMapper userMapper;
    private final UserAuthIdentityMapper userAuthIdentityMapper;
    private final SessionMapper sessionMapper;
    private final UserLoginEventMapper userLoginEventMapper;
    private final UserMembershipMapper userMembershipMapper;
    private final IdGenerator idGenerator;

    MybatisAuthStore(
            EmailVerificationTokenMapper tokenMapper,
            UserMapper userMapper,
            UserAuthIdentityMapper userAuthIdentityMapper,
            SessionMapper sessionMapper,
            UserLoginEventMapper userLoginEventMapper,
            UserMembershipMapper userMembershipMapper,
            IdGenerator idGenerator
    ) {
        this.tokenMapper = tokenMapper;
        this.userMapper = userMapper;
        this.userAuthIdentityMapper = userAuthIdentityMapper;
        this.sessionMapper = sessionMapper;
        this.userLoginEventMapper = userLoginEventMapper;
        this.userMembershipMapper = userMembershipMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public StoredEmailToken saveEmailToken(String email, String purpose, String tokenHash, Instant expiresAt, Instant now) {
        EmailVerificationTokenEntity entity = new EmailVerificationTokenEntity();
        entity.setId(idGenerator.nextId());
        entity.setEmail(email);
        entity.setTokenHash(tokenHash);
        entity.setPurpose(purpose);
        entity.setStatus("active");
        entity.setExpiresAt(ts(expiresAt));
        entity.setCreatedAt(ts(now));
        tokenMapper.insert(entity);
        return toStoredEmailToken(entity);
    }

    @Override
    public Optional<StoredEmailToken> findActiveEmailToken(String email, String purpose, String tokenHash) {
        EmailVerificationTokenEntity entity = tokenMapper.selectOne(
                new LambdaQueryWrapper<EmailVerificationTokenEntity>()
                        .eq(EmailVerificationTokenEntity::getEmail, email)
                        .eq(EmailVerificationTokenEntity::getPurpose, purpose)
                        .eq(EmailVerificationTokenEntity::getTokenHash, tokenHash)
                        .eq(EmailVerificationTokenEntity::getStatus, "active")
                        .orderByDesc(EmailVerificationTokenEntity::getCreatedAt)
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(this::toStoredEmailToken);
    }

    @Override
    public Optional<StoredEmailToken> findLatestEmailToken(String email, String purpose) {
        EmailVerificationTokenEntity entity = tokenMapper.selectOne(
                new LambdaQueryWrapper<EmailVerificationTokenEntity>()
                        .eq(EmailVerificationTokenEntity::getEmail, email)
                        .eq(EmailVerificationTokenEntity::getPurpose, purpose)
                        .orderByDesc(EmailVerificationTokenEntity::getCreatedAt)
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(this::toStoredEmailToken);
    }

    @Override
    public void consumeEmailToken(long tokenId, Instant consumedAt) {
        tokenMapper.update(
                null,
                new LambdaUpdateWrapper<EmailVerificationTokenEntity>()
                        .eq(EmailVerificationTokenEntity::getId, tokenId)
                        .set(EmailVerificationTokenEntity::getStatus, "consumed")
                        .set(EmailVerificationTokenEntity::getConsumedAt, ts(consumedAt))
        );
    }

    @Override
    public Optional<StoredUser> findUserByEmail(String email) {
        return Optional.ofNullable(userMapper.selectByEmail(email)).map(this::toStoredUser);
    }

    @Override
    public Optional<StoredUser> findUserById(long userId) {
        return Optional.ofNullable(userMapper.selectById(userId)).map(this::toStoredUser);
    }

    @Override
    public List<StoredIdentityBinding> findIdentityBindingsByUserId(long userId) {
        return userAuthIdentityMapper.selectList(
                new LambdaQueryWrapper<UserAuthIdentityEntity>()
                        .eq(UserAuthIdentityEntity::getUserId, userId)
                        .isNull(UserAuthIdentityEntity::getDeletedAt)
                        .orderByDesc(UserAuthIdentityEntity::getLastUsedAt)
                        .orderByDesc(UserAuthIdentityEntity::getCreatedAt)
        ).stream().map(this::toStoredIdentityBinding).toList();
    }

    @Override
    public StoredUser createUserWithEmailIdentity(String email, String nickname, Instant now) {
        long userId = idGenerator.nextId();
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setNickname(nickname);
        user.setAccountStatus("active");
        user.setAuthLevel("email_user");
        user.setPrimaryIdentityProvider("email");
        user.setLastLoginProvider("email");
        user.setCreatedAt(ts(now));
        user.setUpdatedAt(ts(now));
        userMapper.insert(user);

        UserAuthIdentityEntity identity = new UserAuthIdentityEntity();
        identity.setId(idGenerator.nextId());
        identity.setUserId(userId);
        identity.setProvider("email");
        identity.setProviderSubject(email);
        identity.setProviderDisplay(email);
        identity.setVerificationStatus("verified");
        identity.setVerifiedAt(ts(now));
        identity.setLastUsedAt(ts(now));
        identity.setCreatedAt(ts(now));
        identity.setUpdatedAt(ts(now));
        userAuthIdentityMapper.insert(identity);
        return toStoredUser(user);
    }

    @Override
    public void markEmailIdentityVerified(long userId, String email, Instant now) {
        userAuthIdentityMapper.update(
                null,
                new LambdaUpdateWrapper<UserAuthIdentityEntity>()
                        .eq(UserAuthIdentityEntity::getUserId, userId)
                        .eq(UserAuthIdentityEntity::getProvider, "email")
                        .eq(UserAuthIdentityEntity::getProviderSubject, email)
                        .set(UserAuthIdentityEntity::getVerificationStatus, "verified")
                        .set(UserAuthIdentityEntity::getVerifiedAt, ts(now))
                        .set(UserAuthIdentityEntity::getLastUsedAt, ts(now))
                        .set(UserAuthIdentityEntity::getUpdatedAt, ts(now))
        );
        userMapper.update(
                null,
                new LambdaUpdateWrapper<UserEntity>()
                        .eq(UserEntity::getId, userId)
                        .set(UserEntity::getAuthLevel, "email_user")
                        .set(UserEntity::getPrimaryIdentityProvider, "email")
                        .set(UserEntity::getLastLoginProvider, "email")
                        .set(UserEntity::getUpdatedAt, ts(now))
        );
    }

    @Override
    public StoredSession saveSession(
            long userId,
            String tokenHash,
            String loginProvider,
            String deviceLabel,
            String ipAddress,
            String ipHash,
            String userAgentHash,
            Instant expiresAt,
            Instant now
    ) {
        SessionEntity entity = new SessionEntity();
        entity.setId(idGenerator.nextId());
        entity.setUserId(userId);
        entity.setSessionTokenHash(tokenHash);
        entity.setStatus("active");
        entity.setLoginProvider(loginProvider);
        entity.setDeviceLabel(deviceLabel);
        entity.setIpAddress(ipAddress);
        entity.setIpHash(ipHash);
        entity.setUserAgentHash(userAgentHash);
        entity.setExpiresAt(ts(expiresAt));
        entity.setLastSeenAt(ts(now));
        entity.setCreatedAt(ts(now));
        entity.setUpdatedAt(ts(now));
        sessionMapper.insert(entity);
        return toStoredSession(entity);
    }

    @Override
    public Optional<StoredSession> findActiveSession(String tokenHash, Instant now) {
        SessionEntity entity = sessionMapper.selectOne(
                new LambdaQueryWrapper<SessionEntity>()
                        .eq(SessionEntity::getSessionTokenHash, tokenHash)
                        .eq(SessionEntity::getStatus, "active")
                        .gt(SessionEntity::getExpiresAt, ts(now))
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(this::toStoredSession);
    }

    @Override
    public Optional<StoredSession> findActiveSessionById(long sessionId, Instant now) {
        SessionEntity entity = sessionMapper.selectOne(
                new LambdaQueryWrapper<SessionEntity>()
                        .eq(SessionEntity::getId, sessionId)
                        .eq(SessionEntity::getStatus, "active")
                        .gt(SessionEntity::getExpiresAt, ts(now))
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(entity).map(this::toStoredSession);
    }

    @Override
    public List<StoredSession> findActiveSessionsByUserId(long userId, Instant now) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<SessionEntity>()
                        .eq(SessionEntity::getUserId, userId)
                        .eq(SessionEntity::getStatus, "active")
                        .gt(SessionEntity::getExpiresAt, ts(now))
                        .orderByDesc(SessionEntity::getCreatedAt)
        ).stream().map(this::toStoredSession).toList();
    }

    @Override
    public void touchSession(long sessionId, Instant now) {
        sessionMapper.update(
                null,
                new LambdaUpdateWrapper<SessionEntity>()
                        .eq(SessionEntity::getId, sessionId)
                        .eq(SessionEntity::getStatus, "active")
                        .set(SessionEntity::getLastSeenAt, ts(now))
                        .set(SessionEntity::getUpdatedAt, ts(now))
        );
    }

    @Override
    public void revokeSession(long sessionId, Instant now) {
        sessionMapper.update(
                null,
                new LambdaUpdateWrapper<SessionEntity>()
                        .eq(SessionEntity::getId, sessionId)
                        .set(SessionEntity::getStatus, "revoked")
                        .set(SessionEntity::getUpdatedAt, ts(now))
        );
    }

    @Override
    public int countEmailTokensCreatedSince(String email, String purpose, Instant since) {
        return Math.toIntExact(tokenMapper.selectCount(
                new LambdaQueryWrapper<EmailVerificationTokenEntity>()
                        .eq(EmailVerificationTokenEntity::getEmail, email)
                        .eq(EmailVerificationTokenEntity::getPurpose, purpose)
                        .ge(EmailVerificationTokenEntity::getCreatedAt, ts(since))
        ));
    }

    @Override
    public void updateUserProfile(long userId, String nickname, String bio, String avatarUrl, Instant now) {
        userMapper.update(
                null,
                new LambdaUpdateWrapper<UserEntity>()
                        .eq(UserEntity::getId, userId)
                        .set(UserEntity::getNickname, nickname)
                        .set(UserEntity::getBio, bio)
                        .set(UserEntity::getAvatarUrl, avatarUrl)
                        .set(UserEntity::getUpdatedAt, ts(now))
        );
    }

    @Override
    public List<StoredLoginEvent> findLoginEventsByUserId(long userId, int limit) {
        return userLoginEventMapper.selectByUserIdOrAnonymous(userId, limit).stream()
                .map(this::toStoredLoginEvent)
                .toList();
    }

    @Override
    public void recordLoginEvent(Long userId, String provider, String eventType, boolean success, String failureReason, String ipAddress, String ipHash, String userAgentHash, Instant now) {
        UserLoginEventEntity entity = new UserLoginEventEntity();
        entity.setId(idGenerator.nextId());
        entity.setUserId(userId);
        entity.setProvider(provider);
        entity.setEventType(eventType);
        entity.setSuccess(success);
        entity.setFailureReason(failureReason);
        entity.setIpAddress(ipAddress);
        entity.setIpHash(ipHash);
        entity.setUserAgentHash(userAgentHash);
        entity.setCreatedAt(ts(now));
        userLoginEventMapper.insert(entity);
    }

    @Override
    public boolean hasActivePremiumMembership(long userId, Instant now) {
        return userMembershipMapper.selectCount(
                new LambdaQueryWrapper<UserMembershipEntity>()
                        .eq(UserMembershipEntity::getUserId, userId)
                        .eq(UserMembershipEntity::getMembershipType, "premium")
                        .eq(UserMembershipEntity::getStatus, "active")
                        .isNull(UserMembershipEntity::getDeletedAt)
                        .and(wrapper -> wrapper.isNull(UserMembershipEntity::getExpiresAt)
                                .or()
                                .gt(UserMembershipEntity::getExpiresAt, ts(now)))
        ) > 0;
    }

    private StoredEmailToken toStoredEmailToken(EmailVerificationTokenEntity entity) {
        return new StoredEmailToken(
                entity.getId(),
                entity.getEmail(),
                entity.getPurpose(),
                entity.getTokenHash(),
                entity.getStatus(),
                instant(entity.getExpiresAt()),
                instant(entity.getConsumedAt()),
                instant(entity.getCreatedAt())
        );
    }

    private StoredUser toStoredUser(UserEntity entity) {
        return new StoredUser(
                entity.getId(),
                entity.getNickname(),
                entity.getAvatarUrl(),
                entity.getBio(),
                entity.getAccountStatus(),
                entity.getAuthLevel(),
                entity.getPrimaryIdentityProvider(),
                entity.getLastLoginProvider()
        );
    }

    private StoredSession toStoredSession(SessionEntity entity) {
        return new StoredSession(
                entity.getId(),
                entity.getUserId(),
                entity.getSessionTokenHash(),
                entity.getStatus(),
                entity.getLoginProvider(),
                entity.getDeviceLabel(),
                entity.getIpAddress(),
                entity.getIpHash(),
                entity.getUserAgentHash(),
                instant(entity.getExpiresAt()),
                instant(entity.getLastSeenAt()),
                instant(entity.getCreatedAt()),
                instant(entity.getUpdatedAt())
        );
    }

    private StoredLoginEvent toStoredLoginEvent(UserLoginEventEntity entity) {
        return new StoredLoginEvent(
                entity.getId(),
                entity.getUserId(),
                entity.getProvider(),
                entity.getEventType(),
                entity.isSuccess(),
                entity.getFailureReason(),
                instant(entity.getCreatedAt())
        );
    }

    private StoredIdentityBinding toStoredIdentityBinding(UserAuthIdentityEntity entity) {
        return new StoredIdentityBinding(
                entity.getProvider(),
                entity.getProviderDisplay(),
                entity.getVerificationStatus(),
                instant(entity.getLastUsedAt())
        );
    }

    private Timestamp ts(Instant instant) {
        return Timestamp.from(instant);
    }

    private Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
