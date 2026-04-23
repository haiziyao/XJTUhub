package org.xjtuhub.common.api;

public record ApiResponse<T>(
        T data,
        ApiError error,
        String requestId,
        long durationMs
) {
    public static <T> ApiResponse<T> ok(T data, String requestId, long durationMs) {
        return new ApiResponse<>(data, null, requestId, durationMs);
    }

    public static <T> ApiResponse<T> fail(ApiError error, String requestId, long durationMs) {
        return new ApiResponse<>(null, error, requestId, durationMs);
    }
}
