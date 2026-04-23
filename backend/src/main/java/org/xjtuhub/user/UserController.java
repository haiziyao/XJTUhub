package org.xjtuhub.user;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xjtuhub.auth.AuthService;
import org.xjtuhub.auth.CurrentUserDto;
import org.xjtuhub.common.api.ApiResponse;
import org.xjtuhub.common.web.RequestContext;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserDto> currentUser(HttpServletRequest request) {
        return ApiResponse.ok(
                authService.requireCurrentUser(request),
                RequestContext.requestId(request),
                RequestContext.durationMs(request)
        );
    }
}
