package org.xjtuhub.search;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    public SearchResponse search(String query, Integer page, Integer pageSize) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        return new SearchResponse(
                query == null ? "" : query,
                List.of(),
                normalizedPage,
                normalizedPageSize,
                0,
                false,
                "placeholder"
        );
    }

    public SearchIndexTaskListResponse listIndexTasks() {
        return new SearchIndexTaskListResponse(List.of(
                new SearchIndexTaskDto(
                        "content_index_sync",
                        "content",
                        "placeholder",
                        "Elasticsearch is not connected yet. Placeholder task state is returned for integration testing."
                )
        ));
    }
}
