package org.xjtuhub.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestTimingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_ATTRIBUTE = "xjtuhub.requestId";
    public static final String START_NANOS_ATTRIBUTE = "xjtuhub.startNanos";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = "req_" + UUID.randomUUID().toString().replace("-", "");
        }

        long startNanos = System.nanoTime();
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        request.setAttribute(START_NANOS_ATTRIBUTE, startNanos);
        response.setHeader("X-Request-Id", requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = elapsedMillis(startNanos);
            response.setHeader("X-Duration-Ms", Long.toString(durationMs));
        }
    }

    public static long elapsedMillis(long startNanos) {
        return Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
    }
}
