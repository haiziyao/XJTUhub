package org.xjtuhub.system;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xjtuhub.common.api.ApiResponse;
import org.xjtuhub.common.web.RequestContext;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ApiResponse<Map<String, String>> health(HttpServletRequest request) {
        return ApiResponse.ok(
                Map.of("status", "ok"),
                RequestContext.requestId(request),
                RequestContext.durationMs(request)
        );
    }
}
