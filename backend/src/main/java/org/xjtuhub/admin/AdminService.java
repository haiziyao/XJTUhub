package org.xjtuhub.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.xjtuhub.auth.AuthProperties;
import org.xjtuhub.auth.AuthStore;
import org.xjtuhub.common.api.BusinessException;
import org.xjtuhub.common.support.TimeProvider;
import org.xjtuhub.common.web.RequestContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

@Service
public class AdminService {
    private final AdminStore adminStore;
    private final AuthStore authStore;
    private final AuthProperties authProperties;
    private final TimeProvider timeProvider;
    private final ObjectMapper objectMapper;

    public AdminService(
            AdminStore adminStore,
            AuthStore authStore,
            AuthProperties authProperties,
            TimeProvider timeProvider,
            ObjectMapper objectMapper
    ) {
        this.adminStore = adminStore;
        this.authStore = authStore;
        this.authProperties = authProperties;
        this.timeProvider = timeProvider;
        this.objectMapper = objectMapper;
    }

    public AdminCampusVerificationResponse markCampusVerification(
            String userId,
            AdminCampusVerificationRequest request,
            HttpServletRequest servletRequest
    ) {
        long actorUserId = requireCurrentUserId(servletRequest);
        AdminStore.StoredAdminAccount adminAccount = adminStore.findActiveAdminByUserId(actorUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.FORBIDDEN, "ADMIN_FORBIDDEN", "Admin permission required."));
        long targetUserId = Long.parseLong(userId);
        AdminStore.StoredUserRecord targetUser = adminStore.findUserById(targetUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));

        Instant now = timeProvider.now();
        adminStore.markCampusVerification(targetUserId, actorUserId, now);
        adminStore.writeAuditLog(
                actorUserId,
                adminAccount.id(),
                "admin_mark_campus_verification",
                "user",
                targetUserId,
                RequestContext.requestId(servletRequest),
                servletRequest.getRemoteAddr(),
                hashNullable(servletRequest.getRemoteAddr()),
                hashNullable(servletRequest.getHeader("User-Agent")),
                auditDetails(request, targetUser.authLevel()),
                now
        );

        return new AdminCampusVerificationResponse(
                userId,
                "campus_app_verified",
                "verified",
                now
        );
    }

    private long requireCurrentUserId(HttpServletRequest request) {
        String token = readCookie(request, authProperties.getSession().getCookieName());
        if (token == null || token.isBlank()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_LOGIN_REQUIRED", "Login required.");
        }
        return authStore.findActiveSession(sha256(token), timeProvider.now())
                .map(AuthStore.StoredSession::userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_LOGIN_REQUIRED", "Login required."));
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

    private String auditDetails(AdminCampusVerificationRequest request, String previousAuthLevel) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "previousAuthLevel", previousAuthLevel,
                    "note", request == null ? null : request.note()
            ));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize admin audit details.", ex);
        }
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
}
