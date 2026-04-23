package org.xjtuhub.system;

public record DependencyHealthStatus(
        String status,
        String detail
) {
    public static DependencyHealthStatus ok(String detail) {
        return new DependencyHealthStatus("ok", detail);
    }

    public static DependencyHealthStatus skipped(String detail) {
        return new DependencyHealthStatus("skipped", detail);
    }

    public static DependencyHealthStatus down(String detail) {
        return new DependencyHealthStatus("down", detail);
    }
}
