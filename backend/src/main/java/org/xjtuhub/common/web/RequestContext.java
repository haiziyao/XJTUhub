package org.xjtuhub.common.web;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestContext {

    private RequestContext() {
    }

    public static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute(RequestTimingFilter.REQUEST_ID_ATTRIBUTE);
        return value instanceof String requestId ? requestId : "req_unknown";
    }

    public static long durationMs(HttpServletRequest request) {
        Object value = request.getAttribute(RequestTimingFilter.START_NANOS_ATTRIBUTE);
        if (value instanceof Long startNanos) {
            return RequestTimingFilter.elapsedMillis(startNanos);
        }
        return 0L;
    }
}
