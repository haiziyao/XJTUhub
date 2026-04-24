package org.xjtuhub.search;

import java.time.Instant;
import java.util.List;

record SearchResponse(
        String query,
        List<SearchHitDto> items,
        int page,
        int pageSize,
        long total,
        boolean hasNext,
        String indexStatus
) {
}

record SearchHitDto(
        String type,
        String id,
        String title,
        String snippet,
        String visibility,
        Instant publishedAt
) {
}

record SearchIndexTaskListResponse(
        List<SearchIndexTaskDto> items
) {
}

record SearchIndexTaskDto(
        String taskType,
        String targetType,
        String status,
        String detail
) {
}
