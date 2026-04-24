package org.xjtuhub.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "xjtuhub.auth.email.debug-return-token=true"
})
@AutoConfigureMockMvc
class AdminFlowTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryAdminStore inMemoryAdminStore;

    @Test
    void adminCanMarkCampusVerificationAndTargetUserSeesUpdatedIdentity() throws Exception {
        Cookie adminSession = loginAndGetSessionCookie("admin@example.com", "admin-device");
        Cookie targetSession = loginAndGetSessionCookie("target@example.com", "target-device");

        String adminUserId = currentUserId(adminSession);
        String targetUserId = currentUserId(targetSession);
        inMemoryAdminStore.grantAdminAccount(Long.parseLong(adminUserId), "super_admin");

        mockMvc.perform(post("/api/v1/admin/users/{userId}/campus-verification", targetUserId)
                        .cookie(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "manual verification"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(targetUserId))
                .andExpect(jsonPath("$.data.authLevel").value("campus_app_verified"))
                .andExpect(jsonPath("$.data.verificationStatus").value("verified"));

        mockMvc.perform(get("/api/v1/users/me").cookie(targetSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authLevel").value("campus_app_verified"))
                .andExpect(jsonPath("$.data.identitySummary").value("校园已认证"))
                .andExpect(jsonPath("$.data.displayBadges[*].code", hasItem("campus_verified")))
                .andExpect(jsonPath("$.data.identityBindings[*].provider", hasItem("campus_app")));
    }

    @Test
    void nonAdminCannotMarkCampusVerification() throws Exception {
        Cookie actorSession = loginAndGetSessionCookie("plain@example.com", "plain-device");
        Cookie targetSession = loginAndGetSessionCookie("plain-target@example.com", "target-device");
        String targetUserId = currentUserId(targetSession);

        mockMvc.perform(post("/api/v1/admin/users/{userId}/campus-verification", targetUserId)
                        .cookie(actorSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "manual verification"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ADMIN_FORBIDDEN"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())));
    }

    @Test
    void adminCanReadCurrentAdminIdentity() throws Exception {
        Cookie adminSession = loginAndGetSessionCookie("admin-me@example.com", "admin-device");
        String adminUserId = currentUserId(adminSession);
        inMemoryAdminStore.grantAdminAccount(Long.parseLong(adminUserId), "super_admin");

        mockMvc.perform(get("/api/v1/admin/me").cookie(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(adminUserId))
                .andExpect(jsonPath("$.data.adminRole").value("super_admin"))
                .andExpect(jsonPath("$.data.status").value("active"))
                .andExpect(jsonPath("$.data.adminAccountId", not(blankOrNullString())))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())));
    }

    @Test
    void nonAdminCannotReadCurrentAdminIdentity() throws Exception {
        Cookie actorSession = loginAndGetSessionCookie("plain-me@example.com", "plain-device");

        mockMvc.perform(get("/api/v1/admin/me").cookie(actorSession))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ADMIN_FORBIDDEN"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())));
    }

    @Test
    void adminCanListAuditLogs() throws Exception {
        Cookie adminSession = loginAndGetSessionCookie("audit-admin@example.com", "admin-device");
        Cookie targetSession = loginAndGetSessionCookie("audit-target@example.com", "target-device");
        String adminUserId = currentUserId(adminSession);
        String targetUserId = currentUserId(targetSession);
        inMemoryAdminStore.grantAdminAccount(Long.parseLong(adminUserId), "super_admin");

        mockMvc.perform(post("/api/v1/admin/users/{userId}/campus-verification", targetUserId)
                        .cookie(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "audit list seed"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .cookie(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.items[0].actorUserId").value(adminUserId))
                .andExpect(jsonPath("$.data.items[0].action").value("admin_mark_campus_verification"))
                .andExpect(jsonPath("$.data.items[0].targetType").value("user"))
                .andExpect(jsonPath("$.data.items[0].targetId").value(targetUserId))
                .andExpect(jsonPath("$.data.items[0].detailsJson", containsString("\"previousAuthLevel\":\"email_user\"")))
                .andExpect(jsonPath("$.data.items[0].detailsJson", containsString("\"note\":\"audit list seed\"")))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())));
    }

    @Test
    void nonAdminCannotListAuditLogs() throws Exception {
        Cookie actorSession = loginAndGetSessionCookie("audit-plain@example.com", "plain-device");

        mockMvc.perform(get("/api/v1/admin/audit-logs").cookie(actorSession))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ADMIN_FORBIDDEN"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())));
    }

    @Test
    void adminCanListCampusVerificationHistoryForUser() throws Exception {
        Cookie adminSession = loginAndGetSessionCookie("history-admin@example.com", "admin-device");
        Cookie targetSession = loginAndGetSessionCookie("history-target@example.com", "target-device");
        Cookie otherTargetSession = loginAndGetSessionCookie("history-other@example.com", "other-device");
        String adminUserId = currentUserId(adminSession);
        String targetUserId = currentUserId(targetSession);
        String otherTargetUserId = currentUserId(otherTargetSession);
        inMemoryAdminStore.grantAdminAccount(Long.parseLong(adminUserId), "super_admin");

        mockMvc.perform(post("/api/v1/admin/users/{userId}/campus-verification", targetUserId)
                        .cookie(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "first target mark"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/users/{userId}/campus-verification", otherTargetUserId)
                        .cookie(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "other target mark"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/users/{userId}/campus-verification", targetUserId)
                        .cookie(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "second target mark"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/users/{userId}/campus-verification/history", targetUserId)
                        .param("page", "1")
                        .param("pageSize", "20")
                        .cookie(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.items[0].actorUserId").value(adminUserId))
                .andExpect(jsonPath("$.data.items[0].action").value("admin_mark_campus_verification"))
                .andExpect(jsonPath("$.data.items[0].targetType").value("user"))
                .andExpect(jsonPath("$.data.items[0].targetId").value(targetUserId))
                .andExpect(jsonPath("$.data.items[0].detailsJson", containsString("\"note\":\"second target mark\"")))
                .andExpect(jsonPath("$.data.items[1].detailsJson", containsString("\"note\":\"first target mark\"")))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())));
    }

    @Test
    void nonAdminCannotListCampusVerificationHistoryForUser() throws Exception {
        Cookie actorSession = loginAndGetSessionCookie("history-plain@example.com", "plain-device");
        Cookie targetSession = loginAndGetSessionCookie("history-plain-target@example.com", "target-device");
        String targetUserId = currentUserId(targetSession);

        mockMvc.perform(get("/api/v1/admin/users/{userId}/campus-verification/history", targetUserId)
                        .cookie(actorSession))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ADMIN_FORBIDDEN"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())));
    }

    @Test
    void adminCanListUserIdentityBindings() throws Exception {
        Cookie adminSession = loginAndGetSessionCookie("identity-admin@example.com", "admin-device");
        Cookie targetSession = loginAndGetSessionCookie("identity-target@example.com", "target-device");
        String adminUserId = currentUserId(adminSession);
        String targetUserId = currentUserId(targetSession);
        inMemoryAdminStore.grantAdminAccount(Long.parseLong(adminUserId), "super_admin");

        mockMvc.perform(post("/api/v1/admin/users/{userId}/campus-verification", targetUserId)
                        .cookie(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "identity binding seed"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/users/{userId}/identity-bindings", targetUserId)
                        .cookie(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(targetUserId))
                .andExpect(jsonPath("$.data.identityBindings[*].provider", hasItem("email")))
                .andExpect(jsonPath("$.data.identityBindings[*].provider", hasItem("campus_app")))
                .andExpect(jsonPath("$.data.identityBindings[?(@.provider == 'campus_app')].verificationStatus", hasItem("verified")))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())));
    }

    @Test
    void nonAdminCannotListUserIdentityBindings() throws Exception {
        Cookie actorSession = loginAndGetSessionCookie("identity-plain@example.com", "plain-device");
        Cookie targetSession = loginAndGetSessionCookie("identity-plain-target@example.com", "target-device");
        String targetUserId = currentUserId(targetSession);

        mockMvc.perform(get("/api/v1/admin/users/{userId}/identity-bindings", targetUserId)
                        .cookie(actorSession))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ADMIN_FORBIDDEN"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())));
    }

    private Cookie loginAndGetSessionCookie(String email, String deviceLabel) throws Exception {
        MvcResult tokenResult = mockMvc.perform(post("/api/v1/auth/email-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "purpose": "login"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        String token = readJson(tokenResult).at("/data/token").asText();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/email-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "purpose": "login",
                                  "token": "%s",
                                  "deviceLabel": "%s"
                                }
                                """.formatted(email, token, deviceLabel)))
                .andExpect(status().isOk())
                .andReturn();
        return loginResult.getResponse().getCookie("XJTUHUB_SESSION");
    }

    private String currentUserId(Cookie sessionCookie) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/users/me").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result).at("/data/id").asText();
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }
}
