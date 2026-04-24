package org.xjtuhub.search;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SearchFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchEndpointReturnsStableEmptyPlaceholderResult() throws Exception {
        mockMvc.perform(get("/api/v1/search").queryParam("q", "xjtu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.query").value("xjtu"))
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.indexStatus").value("placeholder"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())))
                .andExpect(jsonPath("$.durationMs", greaterThanOrEqualTo(0)));
    }

    @Test
    void searchIndexTasksEndpointReturnsPlaceholderTaskState() throws Exception {
        mockMvc.perform(get("/api/v1/search/index-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].taskType").value("content_index_sync"))
                .andExpect(jsonPath("$.data.items[0].status").value("placeholder"))
                .andExpect(jsonPath("$.data.items[0].targetType").value("content"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())))
                .andExpect(jsonPath("$.durationMs", greaterThanOrEqualTo(0)));
    }
}
