package org.xjtuhub.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xjtuhub.common.api.ApiResponse;
import org.xjtuhub.common.web.RequestContext;

@RestController
@RequestMapping("/api/v1/auth/campus-scan")
public class CampusScanController {
    private final CampusScanService campusScanService;

    public CampusScanController(CampusScanService campusScanService) {
        this.campusScanService = campusScanService;
    }

    @PostMapping("/sessions")
    public ApiResponse<Void> createReservedSession(HttpServletRequest request) {
        campusScanService.throwReserved();
        return ApiResponse.ok(null, RequestContext.requestId(request), RequestContext.durationMs(request));
    }

    @GetMapping("/sessions/{sceneId}")
    public ApiResponse<Void> getReservedSession(@PathVariable String sceneId, HttpServletRequest request) {
        campusScanService.throwReserved();
        return ApiResponse.ok(null, RequestContext.requestId(request), RequestContext.durationMs(request));
    }

    @PostMapping("/sessions/{sceneId}/confirm")
    public ApiResponse<Void> confirmReservedSession(@PathVariable String sceneId, HttpServletRequest request) {
        campusScanService.throwReserved();
        return ApiResponse.ok(null, RequestContext.requestId(request), RequestContext.durationMs(request));
    }
}
