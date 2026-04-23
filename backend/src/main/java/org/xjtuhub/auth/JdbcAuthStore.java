package org.xjtuhub.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.xjtuhub.common.support.IdGenerator;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnBean(JdbcTemplate.class)
class JdbcAuthStore implements AuthStore {
    private final JdbcTemplate jdbcTemplate;
    private final IdGenerator idGenerator;

    JdbcAuthStore(JdbcTemplate jdbcTemplate, IdGenerator idGenerator) {
        this.jdbcTemplate = jdbcTemplate;
        this.idGenerator = idGenerator;
    }

    @Override
    public StoredEmailToken saveEmailToken(String email, String purpose, String tokenHash, Instant expiresAt, Instant now) {
        long id = idGenerator.nextId();
        jdbcTemplate.update("""
                INSERT INTO email_verification_tokens (
                    id, email, token_hash, purpose, status, expires_at, created_at
                ) VALUES (?, ?, ?, ?, 'active', ?, ?)
                """, id, email, tokenHash, purpose, ts(expiresAt), ts(now));
        return new StoredEmailToken(id, email, purpose, tokenHash, "active", expiresAt, null, now);
    }

    @Override
    public Optional<StoredEmailToken> findActiveEmailToken(String email, String purpose, String tokenHash) {
        List<StoredEmailToken> results = jdbcTemplate.query("""
                SELECT id, email, purpose, token_hash, status, expires_at, consumed_at, created_at
                FROM email_verification_tokens
                WHERE email = ?
                  AND purpose = ?
                  AND token_hash = ?
                  AND status = 'active'
                ORDER BY created_at DESC
                LIMIT 1
                """, (rs, rowNum) -> new StoredEmailToken(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("purpose"),
                rs.getString("token_hash"),
                rs.getString("status"),
                rs.getTimestamp("expires_at").toInstant(),
                rs.getTimestamp("consumed_at") == null ? null : rs.getTimestamp("consumed_at").toInstant(),
                rs.getTimestamp("created_at").toInstant()
        ), email, purpose, tokenHash);
        return results.stream().findFirst();
    }

    @Override
    public void consumeEmailToken(long tokenId, Instant consumedAt) {
        jdbcTemplate.update("""
                UPDATE email_verification_tokens
                SET status = 'consumed', consumed_at = ?
                WHERE id = ?
                """, ts(consumedAt), tokenId);
    }

    @Override
    public Optional<StoredUser> findUserByEmail(String email) {
        List<StoredUser> results = jdbcTemplate.query("""
                SELECT u.id, u.nickname, u.avatar_url, u.bio, u.account_status, u.auth_level,
                       u.primary_identity_provider, u.last_login_provider
                FROM users u
                JOIN user_auth_identities i ON i.user_id = u.id
                WHERE i.provider = 'email'
                  AND i.provider_subject = ?
                  AND i.deleted_at IS NULL
                  AND u.deleted_at IS NULL
                LIMIT 1
                """, (rs, rowNum) -> new StoredUser(
                rs.getLong("id"),
                rs.getString("nickname"),
                rs.getString("avatar_url"),
                rs.getString("bio"),
                rs.getString("account_status"),
                rs.getString("auth_level"),
                rs.getString("primary_identity_provider"),
                rs.getString("last_login_provider")
        ), email);
        return results.stream().findFirst();
    }

    @Override
    public Optional<StoredUser> findUserById(long userId) {
        List<StoredUser> results = jdbcTemplate.query("""
                SELECT id, nickname, avatar_url, bio, account_status, auth_level,
                       primary_identity_provider, last_login_provider
                FROM users
                WHERE id = ?
                  AND deleted_at IS NULL
                LIMIT 1
                """, (rs, rowNum) -> new StoredUser(
                rs.getLong("id"),
                rs.getString("nickname"),
                rs.getString("avatar_url"),
                rs.getString("bio"),
                rs.getString("account_status"),
                rs.getString("auth_level"),
                rs.getString("primary_identity_provider"),
                rs.getString("last_login_provider")
        ), userId);
        return results.stream().findFirst();
    }

    @Override
    public StoredUser createUserWithEmailIdentity(String email, String nickname, Instant now) {
        long userId = idGenerator.nextId();
        long identityId = idGenerator.nextId();
        jdbcTemplate.update("""
                INSERT INTO users (
                    id, nickname, account_status, auth_level, primary_identity_provider, last_login_provider,
                    created_at, updated_at
                ) VALUES (?, ?, 'active', 'email_user', 'email', 'email', ?, ?)
                """, userId, nickname, ts(now), ts(now));
        jdbcTemplate.update("""
                INSERT INTO user_auth_identities (
                    id, user_id, provider, provider_subject, provider_display,
                    verification_status, verified_at, last_used_at, created_at, updated_at
                ) VALUES (?, ?, 'email', ?, ?, 'verified', ?, ?, ?, ?)
                """, identityId, userId, email, email, ts(now), ts(now), ts(now), ts(now));
        return new StoredUser(userId, nickname, null, null, "active", "email_user", "email", "email");
    }

    @Override
    public void markEmailIdentityVerified(long userId, String email, Instant now) {
        jdbcTemplate.update("""
                UPDATE user_auth_identities
                SET verification_status = 'verified', verified_at = COALESCE(verified_at, ?), last_used_at = ?, updated_at = ?
                WHERE user_id = ?
                  AND provider = 'email'
                  AND provider_subject = ?
                """, ts(now), ts(now), ts(now), userId, email);
        jdbcTemplate.update("""
                UPDATE users
                SET auth_level = 'email_user',
                    primary_identity_provider = COALESCE(primary_identity_provider, 'email'),
                    last_login_provider = 'email',
                    updated_at = ?
                WHERE id = ?
                """, ts(now), userId);
    }

    @Override
    public StoredSession saveSession(long userId, String tokenHash, String loginProvider, String deviceLabel, Instant expiresAt, Instant now) {
        long id = idGenerator.nextId();
        jdbcTemplate.update("""
                INSERT INTO sessions (
                    id, user_id, session_token_hash, status, login_provider, device_label,
                    expires_at, last_seen_at, created_at, updated_at
                ) VALUES (?, ?, ?, 'active', ?, ?, ?, ?, ?, ?)
                """, id, userId, tokenHash, loginProvider, deviceLabel, ts(expiresAt), ts(now), ts(now), ts(now));
        return new StoredSession(id, userId, tokenHash, "active", loginProvider, deviceLabel, expiresAt, now, now);
    }

    @Override
    public Optional<StoredSession> findActiveSession(String tokenHash, Instant now) {
        List<StoredSession> results = jdbcTemplate.query("""
                SELECT id, user_id, session_token_hash, status, login_provider, device_label, expires_at, created_at, updated_at
                FROM sessions
                WHERE session_token_hash = ?
                  AND status = 'active'
                  AND expires_at > ?
                LIMIT 1
                """, (rs, rowNum) -> new StoredSession(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("session_token_hash"),
                rs.getString("status"),
                rs.getString("login_provider"),
                rs.getString("device_label"),
                rs.getTimestamp("expires_at").toInstant(),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        ), tokenHash, ts(now));
        return results.stream().findFirst();
    }

    @Override
    public List<StoredSession> findActiveSessionsByUserId(long userId, Instant now) {
        return jdbcTemplate.query("""
                SELECT id, user_id, session_token_hash, status, login_provider, device_label, expires_at, created_at, updated_at
                FROM sessions
                WHERE user_id = ?
                  AND status = 'active'
                  AND expires_at > ?
                ORDER BY created_at DESC
                """, (rs, rowNum) -> new StoredSession(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("session_token_hash"),
                rs.getString("status"),
                rs.getString("login_provider"),
                rs.getString("device_label"),
                rs.getTimestamp("expires_at").toInstant(),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        ), userId, ts(now));
    }

    @Override
    public void revokeSession(long sessionId, Instant now) {
        jdbcTemplate.update("""
                UPDATE sessions
                SET status = 'revoked', updated_at = ?
                WHERE id = ?
                """, ts(now), sessionId);
    }

    @Override
    public void recordLoginEvent(Long userId, String provider, String eventType, boolean success, String failureReason, String ipAddress, String ipHash, String userAgentHash, Instant now) {
        jdbcTemplate.update("""
                INSERT INTO user_login_events (
                    id, user_id, provider, event_type, success, failure_reason, ip_address, ip_hash, user_agent_hash, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, idGenerator.nextId(), userId, provider, eventType, success, failureReason, ipAddress, ipHash, userAgentHash, ts(now));
    }

    @Override
    public boolean hasActivePremiumMembership(long userId, Instant now) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM user_memberships
                WHERE user_id = ?
                  AND membership_type = 'premium'
                  AND status = 'active'
                  AND deleted_at IS NULL
                  AND (expires_at IS NULL OR expires_at > ?)
                """, Integer.class, userId, ts(now));
        return count != null && count > 0;
    }

    private Timestamp ts(Instant instant) {
        return Timestamp.from(instant);
    }
}
