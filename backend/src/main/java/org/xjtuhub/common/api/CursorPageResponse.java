package org.xjtuhub.common.api;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        String nextCursor,
        boolean hasNext
) {
}
