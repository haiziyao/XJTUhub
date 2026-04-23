package org.xjtuhub.common.api;

import java.util.List;

public record OffsetPageResponse<T>(
        List<T> items,
        int page,
        int pageSize,
        long total,
        boolean hasNext
) {
}
