package org.xjtuhub.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.xjtuhub.common.api.BadgeDto;
import org.xjtuhub.common.api.BusinessException;
import org.xjtuhub.common.api.IdentityBindingDto;
import org.xjtuhub.common.api.OffsetPageResponse;
import org.xjtuhub.common.support.TimeProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

@Service
public class AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthStore authStore;
    private final TimeProvider timeProvider;
    private final AuthProperties authProperties;
    private final EmailSender emailSender;
    private final EmailTokenVerifyAttemptStore emailTokenVerifyAttemptStore;
    private final EmailVerificationCodeStore emailVerificationCodeStore;

    public AuthService(
            AuthStore authStore,
            TimeProvider timeProvider,
            AuthProperties authProperties,
            EmailSender emailSender,
            EmailTokenVerifyAttemptStore emailTokenVerifyAttemptStore,
            EmailVerificationCodeStore emailVerificationCodeStore
    ) {
        this.authStore = authStore;
        this.timeProvider = timeProvider;
        this.authProperties = authProperties;
        this.emailSender = emailSender;
        this.emailTokenVerifyAttemptStore = emailTokenVerifyAttemptStore;
        this.emailVerificationCodeStore = emailVerificationCodeStore;
    }

    public EmailTokenCreateResponse createEmailToken(EmailTokenCreateRequest request, HttpServletRequest servletRequest) {
        Instant now = timeProvider.now();
        String email = normalizeEmail(request.email());
        ensureTokenCreateAllowed(email, request.purpose(), now);
        String rawToken = emailVerificationCode();
        Instant expiresAt = now.plus(Duration.ofMinutes(authProperties.getEmail().getTokenTtlMinutes()));
        String tokenHash = sha256(rawToken);
        authStore.saveEmailToken(email, request.purpose(), tokenHash, expiresAt, now);
        emailVerificationCodeStore.save(email, request.purpose(), tokenHash, Duration.ofMinutes(authProperties.getEmail().getTokenTtlMinutes()));
        if (!authProperties.getEmail().isDebugReturnToken()) {
            emailSender.send(new EmailSender.EmailMessage(
                    email,
                    "XJTUhub 登录验证码",
                    """
                    您好，

                    您正在使用 XJTUhub 进行邮箱登录验证。本次验证码为：

                    %s

                    验证码 5 分钟内有效，仅用于本次登录，请勿转发或泄露给他人。

                    如果这不是您本人发起的操作，可以直接忽略这封邮件。为保障账号安全，建议您确认设备与网络环境是否正常。

                    XJTUhub
                    """.formatted(rawToken)
            ));
        }
        return new EmailTokenCreateResponse(
                email,
                request.purpose(),
                expiresAt,
                authProperties.getEmail().isDebugReturnToken() ? "debug_return" : "accepted",
                authProperties.getEmail().isDebugReturnToken() ? rawToken : null
        );
    }

    public LoginResult createEmailSession(EmailSessionCreateRequest request, HttpServletRequest servletRequest) {
        Instant now = timeProvider.now();
        String email = normalizeEmail(request.email());
        ensureTokenVerifyAllowed(email, request.purpose(), now);
        String requestTokenHash = sha256(request.token());
        String cachedTokenHash = emailVerificationCodeStore.getTokenHash(email, request.purpose());
        Optional<AuthStore.StoredEmailToken> latestToken = authStore.findLatestEmailToken(email, request.purpose());
        if (latestToken.isEmpty()) {
            emailTokenVerifyAttemptStore.recordFailure(email, request.purpose(), now);
            authStore.recordLoginEvent(null, "email", "email_token_login", false, "token_invalid", clientIp(servletRequest), hashNullable(clientIp(servletRequest)), hashNullable(userAgent(servletRequest)), now);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_EMAIL_TOKEN_INVALID", "Email token is invalid.");
        }
        if (latestToken.get().consumedAt() != null) {
            emailTokenVerifyAttemptStore.recordFailure(email, request.purpose(), now);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_EMAIL_TOKEN_CONSUMED", "Email token has already been used.");
        }
        if (!latestToken.get().expiresAt().isAfter(now)) {
            emailTokenVerifyAttemptStore.recordFailure(email, request.purpose(), now);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_EMAIL_TOKEN_EXPIRED", "Email token has expired.");
        }
        if (cachedTokenHash == null || !cachedTokenHash.equals(requestTokenHash) || !latestToken.get().tokenHash().equals(requestTokenHash)) {
            emailTokenVerifyAttemptStore.recordFailure(email, request.purpose(), now);
            authStore.recordLoginEvent(null, "email", "email_token_login", false, "token_invalid", clientIp(servletRequest), hashNullable(clientIp(servletRequest)), hashNullable(userAgent(servletRequest)), now);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_EMAIL_TOKEN_INVALID", "Email token is invalid.");
        }

        AuthStore.StoredUser user = authStore.findUserByEmail(email)
                .orElseGet(() -> authStore.createUserWithEmailIdentity(email, defaultNickname(email), now));
        authStore.consumeEmailToken(latestToken.get().id(), now);
        emailVerificationCodeStore.delete(email, request.purpose());
        authStore.markEmailIdentityVerified(user.id(), email, now);
        emailTokenVerifyAttemptStore.clearFailures(email, request.purpose());

        String rawSessionToken = sessionToken();
        Instant sessionExpiresAt = now.plus(Duration.ofDays(authProperties.getSession().getTtlDays()));
        String ipAddress = clientIp(servletRequest);
        String ipHash = hashNullable(ipAddress);
        String userAgentHash = hashNullable(userAgent(servletRequest));
        AuthStore.StoredSession session = authStore.saveSession(
                user.id(),
                sha256(rawSessionToken),
                "email",
                request.deviceLabel(),
                ipAddress,
                ipHash,
                userAgentHash,
                sessionExpiresAt,
                now
        );
        authStore.recordLoginEvent(user.id(), "email", "email_token_login", true, null, ipAddress, ipHash, userAgentHash, now);

        CurrentUserDto currentUser = currentUserFor(user.id(), session.id());
        AuthSessionDto currentSession = toSessionDto(session, session.id());
        String cookie = ResponseCookie.from(authProperties.getSession().getCookieName(), rawSessionToken)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(authProperties.getSession().getTtlDays()))
                .build()
                .toString();

        return new LoginResult(new EmailSessionCreateResponse(currentUser, currentSession), cookie);
    }

    public CurrentUserDto requireCurrentUser(HttpServletRequest request) {
        AuthStore.StoredSession session = requireSession(request);
        return currentUserFor(session.userId());
    }

    public OffsetPageResponse<AuthSessionDto> listSessions(HttpServletRequest request) {
        AuthStore.StoredSession currentSession = requireSession(request);
        List<AuthSessionDto> items = authStore.findActiveSessionsByUserId(currentSession.userId(), timeProvider.now()).stream()
                .map(session -> toSessionDto(session, currentSession.id()))
                .toList();
        return new OffsetPageResponse<>(items, 1, items.size(), items.size(), false);
    }

    public SessionRevokeResponse revokeCurrentSession(HttpServletRequest request) {
        AuthStore.StoredSession session = requireSession(request);
        authStore.revokeSession(session.id(), timeProvider.now());
        return new SessionRevokeResponse(true);
    }

    public SessionRevokeResponse revokeSessionById(HttpServletRequest request, String sessionId) {
        AuthStore.StoredSession currentSession = requireSession(request);
        long targetSessionId = parseSessionId(sessionId);
        AuthStore.StoredSession targetSession = authStore.findActiveSessionById(targetSessionId, timeProvider.now())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "AUTH_SESSION_EXPIRED", "Session not found."));
        if (targetSession.userId() != currentSession.userId()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN", "Forbidden.");
        }
        authStore.revokeSession(targetSession.id(), timeProvider.now());
        return new SessionRevokeResponse(true);
    }

    public OffsetPageResponse<LoginEventDto> listLoginEvents(HttpServletRequest request) {
        AuthStore.StoredSession session = requireSession(request);
        List<LoginEventDto> items = authStore.findLoginEventsByUserId(session.userId(), 20).stream()
                .map(event -> new LoginEventDto(
                        String.valueOf(event.id()),
                        event.provider(),
                        event.eventType(),
                        event.success(),
                        event.failureReason(),
                        event.createdAt()
                ))
                .toList();
        return new OffsetPageResponse<>(items, 1, items.size(), items.size(), false);
    }

    public CurrentUserDto updateCurrentUserProfile(HttpServletRequest request, String nickname, String bio, String avatarUrl) {
        AuthStore.StoredSession session = requireSession(request);
        String normalizedNickname = nickname.trim();
        String normalizedBio = bio == null || bio.isBlank() ? null : bio.trim();
        String normalizedAvatarUrl = avatarUrl == null || avatarUrl.isBlank() ? null : avatarUrl.trim();
        authStore.updateUserProfile(session.userId(), normalizedNickname, normalizedBio, normalizedAvatarUrl, timeProvider.now());
        return currentUserFor(session.userId());
    }

    public String clearSessionCookie() {
        return ResponseCookie.from(authProperties.getSession().getCookieName(), "")
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build()
                .toString();
    }

    private CurrentUserDto currentUserFor(long userId, long currentSessionId) {
        return currentUserFor(userId);
    }

    private CurrentUserDto currentUserFor(long userId) {
        AuthStore.StoredUser user = authStore.findUserById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));
        List<AuthStore.StoredIdentityBinding> identityBindings = authStore.findIdentityBindingsByUserId(userId);
        boolean premium = authStore.hasActivePremiumMembership(userId, timeProvider.now());
        return new CurrentUserDto(
                String.valueOf(user.id()),
                user.nickname(),
                user.avatarUrl(),
                user.bio(),
                user.authLevel(),
                premium ? "red" : "default",
                user.primaryIdentityProvider(),
                user.lastLoginProvider(),
                identitySummary(user, identityBindings),
                toIdentityBindingDtos(user, identityBindings),
                displayBadges(user, premium)
        );
    }

    private List<BadgeDto> displayBadges(AuthStore.StoredUser user, boolean premium) {
        List<BadgeDto> badges = new java.util.ArrayList<>();
        if ("campus_app_verified".equals(user.authLevel())) {
            badges.add(new BadgeDto("identity", "campus_verified", "Campus Verified", "trusted"));
        } else {
            badges.add(new BadgeDto("identity", "email_verified", "Email Verified", "neutral"));
        }
        if (premium) {
            badges.add(new BadgeDto("membership", "premium", "Premium", "premium"));
        }
        return badges;
    }

    private AuthSessionDto toSessionDto(AuthStore.StoredSession session, long currentSessionId) {
        return new AuthSessionDto(
                String.valueOf(session.id()),
                session.loginProvider(),
                session.deviceLabel(),
                session.createdAt(),
                session.expiresAt(),
                session.lastSeenAt(),
                session.id() == currentSessionId
        );
    }

    private String identitySummary(AuthStore.StoredUser user, List<AuthStore.StoredIdentityBinding> identityBindings) {
        boolean hasCampusVerified = "campus_app_verified".equals(user.authLevel())
                || identityBindings.stream().anyMatch(binding ->
                "campus_app".equals(binding.provider()) && "verified".equals(binding.verificationStatus()));
        if (hasCampusVerified) {
            return "校园已认证";
        }
        boolean hasCampusBound = identityBindings.stream().anyMatch(binding ->
                "campus_app".equals(binding.provider()) && !"revoked".equals(binding.verificationStatus()));
        boolean hasEmailVerified = identityBindings.stream().anyMatch(binding ->
                "email".equals(binding.provider()) && "verified".equals(binding.verificationStatus()));
        if (hasCampusBound && hasEmailVerified) {
            return "邮箱已验证（已绑定校园）";
        }
        if (hasEmailVerified) {
            return "邮箱已验证";
        }
        return "邮箱未验证";
    }

    private List<IdentityBindingDto> toIdentityBindingDtos(AuthStore.StoredUser user, List<AuthStore.StoredIdentityBinding> identityBindings) {
        return identityBindings.stream()
                .map(binding -> new IdentityBindingDto(
                        binding.provider(),
                        binding.providerDisplay(),
                        binding.verificationStatus(),
                        binding.provider().equals(user.primaryIdentityProvider()),
                        binding.provider().equals(user.lastLoginProvider())
                ))
                .toList();
    }

    private AuthStore.StoredSession requireSession(HttpServletRequest request) {
        String token = readCookie(request, authProperties.getSession().getCookieName());
        if (token == null || token.isBlank()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_LOGIN_REQUIRED", "Login required.");
        }
        Instant now = timeProvider.now();
        AuthStore.StoredSession session = authStore.findActiveSession(sha256(token), now)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_LOGIN_REQUIRED", "Login required."));
        authStore.touchSession(session.id(), now);
        return authStore.findActiveSessionById(session.id(), now).orElse(
                new AuthStore.StoredSession(
                        session.id(),
                        session.userId(),
                        session.sessionTokenHash(),
                session.status(),
                session.loginProvider(),
                session.deviceLabel(),
                session.ipAddress(),
                session.ipHash(),
                session.userAgentHash(),
                session.expiresAt(),
                now,
                session.createdAt(),
                        now
                )
        );
    }

    private void ensureTokenCreateAllowed(String email, String purpose, Instant now) {
        Instant windowStart = now.minusSeconds(authProperties.getEmail().getTokenCreateWindowSeconds());
        int count = authStore.countEmailTokensCreatedSince(email, purpose, windowStart);
        if (count >= authProperties.getEmail().getTokenCreateLimit()) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests.");
        }
    }

    private void ensureTokenVerifyAllowed(String email, String purpose, Instant now) {
        Instant windowStart = now.minusSeconds(authProperties.getEmail().getTokenVerifyWindowSeconds());
        int count = emailTokenVerifyAttemptStore.countRecentFailures(email, purpose, windowStart);
        if (count >= authProperties.getEmail().getTokenVerifyLimit()) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests.");
        }
    }

    private long parseSessionId(String sessionId) {
        try {
            return Long.parseLong(sessionId);
        } catch (NumberFormatException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed.",
                    java.util.Map.of("fields", java.util.Map.of("sessionId", "sessionId must be a numeric string.")));
        }
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String defaultNickname(String email) {
        String local = email.substring(0, email.indexOf('@')).trim();
        return local.isBlank() ? "user" : local;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String sessionToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }

    private String emailVerificationCode() {
        return "%06d".formatted(SECURE_RANDOM.nextInt(1_000_000));
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private String hashNullable(String value) {
        return value == null ? null : sha256(value);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public record LoginResult(
            EmailSessionCreateResponse response,
            String setCookieHeader
    ) {
    }
}
