package org.xjtuhub.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.xjtuhub.common.support.IdGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnMissingBean(JdbcTemplate.class)
class InMemoryAuthStore implements AuthStore {
    private final IdGenerator idGenerator;
    private final Map<Long, StoredEmailToken> tokens = new ConcurrentHashMap<>();
    private final Map<Long, StoredUser> users = new ConcurrentHashMap<>();
    private final Map<String, Long> userIdByEmail = new ConcurrentHashMap<>();
    private final Map<Long, StoredSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, MembershipRecord> memberships = new ConcurrentHashMap<>();

    InMemoryAuthStore(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public StoredEmailToken saveEmailToken(String email, String purpose, String tokenHash, Instant expiresAt, Instant now) {
        StoredEmailToken token = new StoredEmailToken(idGenerator.nextId(), email, purpose, tokenHash, "active", expiresAt, null, now);
        tokens.put(token.id(), token);
        return token;
    }

    @Override
    public Optional<StoredEmailToken> findActiveEmailToken(String email, String purpose, String tokenHash) {
        return tokens.values().stream()
                .filter(token -> token.email().equals(email))
                .filter(token -> token.purpose().equals(purpose))
                .filter(token -> token.tokenHash().equals(tokenHash))
                .filter(token -> token.consumedAt() == null)
                .filter(token -> "active".equals(token.status()))
                .max(Comparator.comparing(StoredEmailToken::createdAt));
    }

    @Override
    public void consumeEmailToken(long tokenId, Instant consumedAt) {
        StoredEmailToken token = tokens.get(tokenId);
        if (token == null) {
            return;
        }
        tokens.put(tokenId, new StoredEmailToken(
                token.id(),
                token.email(),
                token.purpose(),
                token.tokenHash(),
                "consumed",
                token.expiresAt(),
                consumedAt,
                token.createdAt()
        ));
    }

    @Override
    public Optional<StoredUser> findUserByEmail(String email) {
        Long userId = userIdByEmail.get(email);
        return userId == null ? Optional.empty() : Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<StoredUser> findUserById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public StoredUser createUserWithEmailIdentity(String email, String nickname, Instant now) {
        StoredUser user = new StoredUser(idGenerator.nextId(), nickname, null, null, "active", "email_user", "email", "email");
        users.put(user.id(), user);
        userIdByEmail.put(email, user.id());
        return user;
    }

    @Override
    public void markEmailIdentityVerified(long userId, String email, Instant now) {
        userIdByEmail.put(email, userId);
        StoredUser user = users.get(userId);
        if (user == null) {
            return;
        }
        users.put(userId, new StoredUser(
                user.id(),
                user.nickname(),
                user.avatarUrl(),
                user.bio(),
                user.accountStatus(),
                "email_user",
                user.primaryIdentityProvider() == null ? "email" : user.primaryIdentityProvider(),
                "email"
        ));
    }

    @Override
    public StoredSession saveSession(long userId, String tokenHash, String loginProvider, String deviceLabel, Instant expiresAt, Instant now) {
        StoredSession session = new StoredSession(idGenerator.nextId(), userId, tokenHash, "active", loginProvider, deviceLabel, expiresAt, now, now);
        sessions.put(session.id(), session);
        return session;
    }

    @Override
    public Optional<StoredSession> findActiveSession(String tokenHash, Instant now) {
        return sessions.values().stream()
                .filter(session -> session.sessionTokenHash().equals(tokenHash))
                .filter(session -> "active".equals(session.status()))
                .filter(session -> session.expiresAt().isAfter(now))
                .findFirst();
    }

    @Override
    public Optional<StoredSession> findActiveSessionById(long sessionId, Instant now) {
        StoredSession session = sessions.get(sessionId);
        if (session == null || !"active".equals(session.status()) || !session.expiresAt().isAfter(now)) {
            return Optional.empty();
        }
        return Optional.of(session);
    }

    @Override
    public List<StoredSession> findActiveSessionsByUserId(long userId, Instant now) {
        return sessions.values().stream()
                .filter(session -> session.userId() == userId)
                .filter(session -> "active".equals(session.status()))
                .filter(session -> session.expiresAt().isAfter(now))
                .sorted(Comparator.comparing(StoredSession::createdAt).reversed())
                .toList();
    }

    @Override
    public void revokeSession(long sessionId, Instant now) {
        StoredSession session = sessions.get(sessionId);
        if (session == null) {
            return;
        }
        sessions.put(sessionId, new StoredSession(
                session.id(),
                session.userId(),
                session.sessionTokenHash(),
                "revoked",
                session.loginProvider(),
                session.deviceLabel(),
                session.expiresAt(),
                session.createdAt(),
                now
        ));
    }

    @Override
    public int countEmailTokensCreatedSince(String email, String purpose, Instant since) {
        return Math.toIntExact(tokens.values().stream()
                .filter(token -> token.email().equals(email))
                .filter(token -> token.purpose().equals(purpose))
                .filter(token -> !token.createdAt().isBefore(since))
                .count());
    }

    @Override
    public void recordLoginEvent(Long userId, String provider, String eventType, boolean success, String failureReason, String ipAddress, String ipHash, String userAgentHash, Instant now) {
        // Intentionally kept as an in-memory no-op for tests.
    }

    @Override
    public boolean hasActivePremiumMembership(long userId, Instant now) {
        return memberships.values().stream()
                .anyMatch(record -> record.userId == userId
                        && "active".equals(record.status)
                        && (record.expiresAt == null || record.expiresAt.isAfter(now)));
    }

    private record MembershipRecord(long userId, String status, Instant expiresAt) {
    }
}
