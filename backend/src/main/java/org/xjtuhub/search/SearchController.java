package org.xjtuhub.search;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xjtuhub.common.api.ApiResponse;
import org.xjtuhub.common.web.RequestContext;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ApiResponse<SearchResponse> search(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request
    ) {
        return ApiResponse.ok(
                searchService.search(query, page, pageSize),
                RequestContext.requestId(request),
                RequestContext.durationMs(request)
        );
    }

    @GetMapping("/index-tasks")
    public ApiResponse<SearchIndexTaskListResponse> indexTasks(HttpServletRequest request) {
        return ApiResponse.ok(
                searchService.listIndexTasks(),
                RequestContext.requestId(request),
                RequestContext.durationMs(request)
        );
    }
}
