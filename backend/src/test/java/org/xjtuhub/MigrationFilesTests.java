package org.xjtuhub;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationFilesTests {

    private static final Path MIGRATION_DIR = Path.of("src", "main", "resources", "db", "migration");

    @Test
    void flywayMigrationsExistInExpectedOrder() throws IOException {
        List<String> names = Files.list(MIGRATION_DIR)
                .map(path -> path.getFileName().toString())
                .sorted(Comparator.comparingInt(MigrationFilesTests::migrationVersion))
                .toList();

        assertThat(names).containsExactly(
                "V1__init_user_auth.sql",
                "V2__init_admin_organization.sql",
                "V3__init_content.sql",
                "V4__init_comment_reaction.sql",
                "V5__init_file_storage.sql",
                "V6__init_moderation.sql",
                "V7__init_notification.sql",
                "V8__init_audit_search_reserved.sql",
                "V9__init_views.sql",
                "V10__seed_initial_taxonomy.sql"
        );
    }

    @Test
    void migrationsDoNotUseDatabaseSpecificUseStatementsOrForeignKeys() throws IOException {
        try (var paths = Files.list(MIGRATION_DIR)) {
            for (Path path : paths.filter(path -> path.toString().endsWith(".sql")).toList()) {
                String sql = Files.readString(path, StandardCharsets.UTF_8).toLowerCase();

                assertThat(sql)
                        .as(path.getFileName() + " must not hard-code a database with USE")
                        .doesNotContain("use xjtuhub");
                assertThat(sql)
                        .as(path.getFileName() + " must not create MySQL foreign keys")
                        .doesNotContain("foreign key")
                        .doesNotContain(" references ");
            }
        }
    }

    private static int migrationVersion(String fileName) {
        int start = fileName.indexOf('V') + 1;
        int end = fileName.indexOf("__");
        return Integer.parseInt(fileName.substring(start, end));
    }
}
