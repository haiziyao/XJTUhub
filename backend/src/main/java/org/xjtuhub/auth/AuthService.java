package org.xjtuhub.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.xjtuhub.common.api.BadgeDto;
import org.xjtuhub.common.api.BusinessException;
import org.xjtuhub.common.api.OffsetPageResponse;
import org.xjtuhub.common.support.TimeProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

@Service
public class AuthService {
    private final AuthStore authStore;
    private final TimeProvider timeProvider;
    private final AuthProperties authProperties;
    private final EmailSender emailSender;
    private final EmailTokenVerifyAttemptStore emailTokenVerifyAttemptStore;

    public AuthService(
            AuthStore authStore,
            TimeProvider timeProvider,
            AuthProperties authProperties,
            EmailSender emailSender,
            EmailTokenVerifyAttemptStore emailTokenVerifyAttemptStore
    ) {
        this.authStore = authStore;
        this.timeProvider = timeProvider;
        this.authProperties = authProperties;
        this.emailSender = emailSender;
        this.emailTokenVerifyAttemptStore = emailTokenVerifyAttemptStore;
    }

    public EmailTokenCreateResponse createEmailToken(EmailTokenCreateRequest request, HttpServletRequest servletRequest) {
        Instant now = timeProvider.now();
        String email = normalizeEmail(request.email());
        ensureTokenCreateAllowed(email, request.purpose(), now);
        String rawToken = randomToken();
        Instant expiresAt = now.plus(Duration.ofMinutes(authProperties.getEmail().getTokenTtlMinutes()));
        authStore.saveEmailToken(email, request.purpose(), sha256(rawToken), expiresAt, now);
        if (!authProperties.getEmail().isDebugReturnToken()) {
            emailSender.send(new EmailSender.EmailMessage(
                    email,
                    "XJTUhub login token",
                    "Your verification token is: " + rawToken
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
        Optional<AuthStore.StoredEmailToken> token = authStore.findActiveEmailToken(email, request.purpose(), sha256(request.token()));
        if (token.isEmpty()) {
            emailTokenVerifyAttemptStore.recordFailure(email, request.purpose(), now);
            authStore.recordLoginEvent(null, "email", "email_token_login", false, "token_invalid", clientIp(servletRequest), hashNullable(clientIp(servletRequest)), hashNullable(userAgent(servletRequest)), now);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_EMAIL_TOKEN_INVALID", "Email token is invalid.");
        }
        if (token.get().consumedAt() != null) {
            emailTokenVerifyAttemptStore.recordFailure(email, request.purpose(), now);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_EMAIL_TOKEN_CONSUMED", "Email token has already been used.");
        }
        if (!token.get().expiresAt().isAfter(now)) {
            emailTokenVerifyAttemptStore.recordFailure(email, request.purpose(), now);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_EMAIL_TOKEN_EXPIRED", "Email token has expired.");
        }

        AuthStore.StoredUser user = authStore.findUserByEmail(email)
                .orElseGet(() -> authStore.createUserWithEmailIdentity(email, defaultNickname(email), now));
        authStore.consumeEmailToken(token.get().id(), now);
        authStore.markEmailIdentityVerified(user.id(), email, now);
        emailTokenVerifyAttemptStore.clearFailures(email, request.purpose());

        String rawSessionToken = randomToken() + randomToken();
        Instant sessionExpiresAt = now.plus(Duration.ofDays(authProperties.getSession().getTtlDays()));
        AuthStore.StoredSession session = authStore.saveSession(user.id(), sha256(rawSessionToken), "email", request.deviceLabel(), sessionExpiresAt, now);
        authStore.recordLoginEvent(user.id(), "email", "email_token_login", true, null, clientIp(servletRequest), hashNullable(clientIp(servletRequest)), hashNullable(userAgent(servletRequest)), now);

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

    private String randomToken() {
        return Long.toHexString(Double.doubleToLongBits(Math.random())) + Long.toHexString(System.nanoTime());
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
