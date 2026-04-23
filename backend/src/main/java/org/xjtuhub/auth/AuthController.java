package org.xjtuhub.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xjtuhub.common.api.ApiResponse;
import org.xjtuhub.common.api.OffsetPageResponse;
import org.xjtuhub.common.web.RequestContext;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/email-tokens")
    public ApiResponse<EmailTokenCreateResponse> createEmailToken(
            @Valid @RequestBody EmailTokenCreateRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok(
                authService.createEmailToken(request, servletRequest),
                RequestContext.requestId(servletRequest),
                RequestContext.durationMs(servletRequest)
        );
    }

    @PostMapping("/email-sessions")
    public ApiResponse<EmailSessionCreateResponse> createEmailSession(
            @Valid @RequestBody EmailSessionCreateRequest request,
            HttpServletRequest servletRequest,
            jakarta.servlet.http.HttpServletResponse servletResponse
    ) {
        AuthService.LoginResult result = authService.createEmailSession(request, servletRequest);
        servletResponse.addHeader("Set-Cookie", result.setCookieHeader());
        return ApiResponse.ok(
                result.response(),
                RequestContext.requestId(servletRequest),
                RequestContext.durationMs(servletRequest)
        );
    }

    @GetMapping("/sessions")
    public ApiResponse<OffsetPageResponse<AuthSessionDto>> listSessions(HttpServletRequest servletRequest) {
        return ApiResponse.ok(
                authService.listSessions(servletRequest),
                RequestContext.requestId(servletRequest),
                RequestContext.durationMs(servletRequest)
        );
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<SessionRevokeResponse> revokeSessionById(
            @PathVariable String sessionId,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok(
                authService.revokeSessionById(servletRequest, sessionId),
                RequestContext.requestId(servletRequest),
                RequestContext.durationMs(servletRequest)
        );
    }

    @DeleteMapping("/sessions/current")
    public ApiResponse<SessionRevokeResponse> revokeCurrentSession(
            HttpServletRequest servletRequest,
            jakarta.servlet.http.HttpServletResponse servletResponse
    ) {
        servletResponse.addHeader("Set-Cookie", authService.clearSessionCookie());
        return ApiResponse.ok(
                authService.revokeCurrentSession(servletRequest),
                RequestContext.requestId(servletRequest),
                RequestContext.durationMs(servletRequest)
        );
    }
}
