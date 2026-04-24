package org.xjtuhub.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xjtuhub.common.api.ApiResponse;
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

    @PostMapping("/users/{userId}/campus-verification")
    public ApiResponse<Void> markCampusVerificationReserved(
            @PathVariable @SnowflakeId(message = "userId must be a numeric string.") String userId,
            HttpServletRequest request
    ) {
        adminService.throwCampusVerificationReserved();
        return ApiResponse.ok(null, RequestContext.requestId(request), RequestContext.durationMs(request));
    }
}
