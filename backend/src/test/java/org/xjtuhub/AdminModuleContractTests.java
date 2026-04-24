package org.xjtuhub;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminModuleContractTests {

    @Test
    void adminReservedCampusVerificationClassesExist() {
        assertThat(classExists("org.xjtuhub.admin.AdminController")).isTrue();
        assertThat(classExists("org.xjtuhub.admin.AdminService")).isTrue();
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
