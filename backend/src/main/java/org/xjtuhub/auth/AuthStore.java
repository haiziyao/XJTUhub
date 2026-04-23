package org.xjtuhub.auth;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface AuthStore {
    StoredEmailToken saveEmailToken(String email, String purpose, String tokenHash, Instant expiresAt, Instant now);

    Optional<StoredEmailToken> findActiveEmailToken(String email, String purpose, String tokenHash);

    void consumeEmailToken(long tokenId, Instant consumedAt);

    Optional<StoredUser> findUserByEmail(String email);

    Optional<StoredUser> findUserById(long userId);

    List<StoredIdentityBinding> findIdentityBindingsByUserId(long userId);

    StoredUser createUserWithEmailIdentity(String email, String nickname, Instant now);

    void markEmailIdentityVerified(long userId, String email, Instant now);

    StoredSession saveSession(
            long userId,
            String tokenHash,
            String loginProvider,
            String deviceLabel,
            String ipAddress,
            String ipHash,
            String userAgentHash,
            Instant expiresAt,
            Instant now
    );

    Optional<StoredSession> findActiveSession(String tokenHash, Instant now);

    Optional<StoredSession> findActiveSessionById(long sessionId, Instant now);

    List<StoredSession> findActiveSessionsByUserId(long userId, Instant now);

    void touchSession(long sessionId, Instant now);

    void revokeSession(long sessionId, Instant now);

    int countEmailTokensCreatedSince(String email, String purpose, Instant since);

    void updateUserProfile(long userId, String nickname, String bio, String avatarUrl, Instant now);

    List<StoredLoginEvent> findLoginEventsByUserId(long userId, int limit);

    void recordLoginEvent(Long userId, String provider, String eventType, boolean success, String failureReason, String ipAddress, String ipHash, String userAgentHash, Instant now);

    boolean hasActivePremiumMembership(long userId, Instant now);

    record StoredEmailToken(
            long id,
            String email,
            String purpose,
            String tokenHash,
            String status,
            Instant expiresAt,
            Instant consumedAt,
            Instant createdAt
    ) {
    }

    record StoredUser(
            long id,
            String nickname,
            String avatarUrl,
            String bio,
            String accountStatus,
            String authLevel,
            String primaryIdentityProvider,
            String lastLoginProvider
    ) {
    }

    record StoredSession(
            long id,
            long userId,
            String sessionTokenHash,
            String status,
            String loginProvider,
            String deviceLabel,
            String ipAddress,
            String ipHash,
            String userAgentHash,
            Instant expiresAt,
            Instant lastSeenAt,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    record StoredLoginEvent(
            long id,
            Long userId,
            String provider,
            String eventType,
            boolean success,
            String failureReason,
            Instant createdAt
    ) {
    }

    record StoredIdentityBinding(
            String provider,
            String providerDisplay,
            String verificationStatus,
            Instant lastUsedAt
    ) {
    }
}
