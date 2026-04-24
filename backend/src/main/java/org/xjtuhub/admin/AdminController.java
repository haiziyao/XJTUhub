package org.xjtuhub.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xjtuhub.common.api.ApiResponse;
import org.xjtuhub.common.api.OffsetPageResponse;
import org.xjtuhub.common.validation.SnowflakeId;
import org.xjtuhub.common.web.RequestContext;

@Validated
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentAdminResponse> currentAdmin(HttpServletRequest request) {
        return ApiResponse.ok(
                adminService.currentAdmin(request),
                RequestContext.requestId(request),
                RequestContext.durationMs(request)
        );
    }

    @GetMapping("/audit-logs")
    public ApiResponse<OffsetPageResponse<AdminAuditLogDto>> auditLogs(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be at least 1.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be at least 1.") @Max(value = 50, message = "pageSize must be at most 50.") int pageSize,
            HttpServletRequest request
    ) {
        return ApiResponse.ok(
                adminService.listAuditLogs(request, page, pageSize),
                RequestContext.requestId(request),
                RequestContext.durationMs(request)
        );
    }

    @GetMapping("/users/{userId}/campus-verification/history")
    public ApiResponse<OffsetPageResponse<AdminAuditLogDto>> campusVerificationHistory(
            @PathVariable @SnowflakeId(message = "userId must be a numeric string.") String userId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be at least 1.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be at least 1.") @Max(value = 50, message = "pageSize must be at most 50.") int pageSize,
            HttpServletRequest request
    ) {
        return ApiResponse.ok(
                adminService.listCampusVerificationHistory(request, userId, page, pageSize),
                RequestContext.requestId(request),
                RequestContext.durationMs(request)
        );
    }

    @GetMapping("/users/{userId}/identity-bindings")
    public ApiResponse<AdminUserIdentityBindingsResponse> userIdentityBindings(
            @PathVariable @SnowflakeId(message = "userId must be a numeric string.") String userId,
            HttpServletRequest request
    ) {
        return ApiResponse.ok(
                adminService.listUserIdentityBindings(request, userId),
                RequestContext.requestId(request),
                RequestContext.durationMs(request)
        );
    }

    @PostMapping("/users/{userId}/campus-verification")
    public ApiResponse<AdminCampusVerificationResponse> markCampusVerification(
            @PathVariable @SnowflakeId(message = "userId must be a numeric string.") String userId,
            @RequestBody(required = false) AdminCampusVerificationRequest body,
            HttpServletRequest request
    ) {
        return ApiResponse.ok(
                adminService.markCampusVerification(userId, body, request),
                RequestContext.requestId(request),
                RequestContext.durationMs(request)
        );
    }
}
