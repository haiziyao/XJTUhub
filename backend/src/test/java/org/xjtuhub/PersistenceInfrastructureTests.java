package org.xjtuhub;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceInfrastructureTests {

    @Test
    void authPersistenceUsesMybatisStoreInsteadOfJdbcStore() {
        assertThat(classExists("org.xjtuhub.auth.MybatisAuthStore")).isTrue();
        assertThat(classExists("org.xjtuhub.auth.JdbcAuthStore")).isFalse();
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
