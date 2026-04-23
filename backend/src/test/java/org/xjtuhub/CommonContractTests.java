package org.xjtuhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.xjtuhub.common.api.CursorPageResponse;
import org.xjtuhub.common.api.OffsetPageResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommonContractTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void offsetPageResponseUsesGlobalFieldNames() throws Exception {
        OffsetPageResponse<String> response = new OffsetPageResponse<>(List.of("a", "b"), 1, 20, 40, true);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"items\":[\"a\",\"b\"]");
        assertThat(json).contains("\"page\":1");
        assertThat(json).contains("\"pageSize\":20");
        assertThat(json).contains("\"total\":40");
        assertThat(json).contains("\"hasNext\":true");
    }

    @Test
    void cursorPageResponseUsesGlobalFieldNames() throws Exception {
        CursorPageResponse<String> response = new CursorPageResponse<>(List.of("a"), "next_123", true);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"items\":[\"a\"]");
        assertThat(json).contains("\"nextCursor\":\"next_123\"");
        assertThat(json).contains("\"hasNext\":true");
    }

    @Test
    void modularMonolithSkeletonPackagesExist() {
        assertThat(classExists("org.xjtuhub.auth.AuthModule")).isTrue();
        assertThat(classExists("org.xjtuhub.user.UserModule")).isTrue();
        assertThat(classExists("org.xjtuhub.admin.AdminModule")).isTrue();
        assertThat(classExists("org.xjtuhub.organization.OrganizationModule")).isTrue();
        assertThat(classExists("org.xjtuhub.content.ContentModule")).isTrue();
        assertThat(classExists("org.xjtuhub.comment.CommentModule")).isTrue();
        assertThat(classExists("org.xjtuhub.reaction.ReactionModule")).isTrue();
        assertThat(classExists("org.xjtuhub.filestorage.FileStorageModule")).isTrue();
        assertThat(classExists("org.xjtuhub.review.ReviewModule")).isTrue();
        assertThat(classExists("org.xjtuhub.search.SearchModule")).isTrue();
        assertThat(classExists("org.xjtuhub.notification.NotificationModule")).isTrue();
        assertThat(classExists("org.xjtuhub.audit.AuditModule")).isTrue();
    }

    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
