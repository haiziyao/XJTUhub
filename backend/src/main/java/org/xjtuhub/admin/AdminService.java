package org.xjtuhub.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.xjtuhub.auth.AuthProperties;
import org.xjtuhub.auth.AuthStore;
import org.xjtuhub.common.api.BusinessException;
import org.xjtuhub.common.api.IdentityBindingDto;
import org.xjtuhub.common.api.OffsetPageResponse;
import org.xjtuhub.common.support.TimeProvider;
import org.xjtuhub.common.web.RequestContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private static final String CAMPUS_VERIFICATION_ACTION = "admin_mark_campus_verification";
    private static final String USER_TARGET_TYPE = "user";

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
        AdminStore.StoredAdminAccount adminAccount = requireActiveAdmin(actorUserId);
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

    public CurrentAdminResponse currentAdmin(HttpServletRequest servletRequest) {
        long actorUserId = requireCurrentUserId(servletRequest);
        AdminStore.StoredAdminAccount adminAccount = requireActiveAdmin(actorUserId);
        return new CurrentAdminResponse(
                Long.toString(adminAccount.id()),
                Long.toString(adminAccount.userId()),
                adminAccount.adminRole(),
                adminAccount.status()
        );
    }

    public OffsetPageResponse<AdminAuditLogDto> listAuditLogs(HttpServletRequest servletRequest, int page, int pageSize) {
        long actorUserId = requireCurrentUserId(servletRequest);
        requireActiveAdmin(actorUserId);
        int offset = (page - 1) * pageSize;
        long total = adminStore.countAuditLogs();
        List<AdminAuditLogDto> items = adminStore.listAuditLogs(offset, pageSize).stream()
                .map(this::toAuditLogDto)
                .toList();
        return new OffsetPageResponse<>(items, page, pageSize, total, (long) page * pageSize < total);
    }

    public OffsetPageResponse<AdminAuditLogDto> listCampusVerificationHistory(
            HttpServletRequest servletRequest,
            String userId,
            int page,
            int pageSize
    ) {
        long actorUserId = requireCurrentUserId(servletRequest);
        requireActiveAdmin(actorUserId);
        long targetUserId = Long.parseLong(userId);
        int offset = (page - 1) * pageSize;
        long total = adminStore.countAuditLogsByActionAndTarget(CAMPUS_VERIFICATION_ACTION, USER_TARGET_TYPE, targetUserId);
        List<AdminAuditLogDto> items = adminStore.listAuditLogsByActionAndTarget(CAMPUS_VERIFICATION_ACTION, USER_TARGET_TYPE, targetUserId, offset, pageSize).stream()
                .map(this::toAuditLogDto)
                .toList();
        return new OffsetPageResponse<>(items, page, pageSize, total, (long) page * pageSize < total);
    }

    public AdminUserIdentityBindingsResponse listUserIdentityBindings(HttpServletRequest servletRequest, String userId) {
        long actorUserId = requireCurrentUserId(servletRequest);
        requireActiveAdmin(actorUserId);
        long targetUserId = Long.parseLong(userId);
        AdminStore.StoredUserRecord targetUser = adminStore.findUserById(targetUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));
        AuthStore.StoredUser authUser = authStore.findUserById(targetUser.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));
        List<IdentityBindingDto> bindings = authStore.findIdentityBindingsByUserId(targetUser.id()).stream()
                .map(binding -> new IdentityBindingDto(
                        binding.provider(),
                        binding.providerDisplay(),
                        binding.verificationStatus(),
                        binding.provider().equals(authUser.primaryIdentityProvider()),
                        binding.provider().equals(authUser.lastLoginProvider())
                ))
                .toList();
        return new AdminUserIdentityBindingsResponse(userId, bindings);
    }

    private AdminStore.StoredAdminAccount requireActiveAdmin(long userId) {
        return adminStore.findActiveAdminByUserId(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.FORBIDDEN, "ADMIN_FORBIDDEN", "Admin permission required."));
    }

    private String nullableLong(Long value) {
        return value == null ? null : Long.toString(value);
    }

    private AdminAuditLogDto toAuditLogDto(AdminStore.StoredAuditLog log) {
        return new AdminAuditLogDto(
                Long.toString(log.id()),
                nullableLong(log.actorUserId()),
                nullableLong(log.adminAccountId()),
                log.action(),
                log.targetType(),
                nullableLong(log.targetId()),
                log.requestId(),
                log.ipHash(),
                log.userAgentHash(),
                log.detailsJson(),
                log.createdAt()
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
