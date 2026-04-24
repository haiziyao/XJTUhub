package org.xjtuhub;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "xjtuhub.auth.email.debug-return-token=true"
})
@AutoConfigureMockMvc
class AuthFlowTests {
    private static final String PRIMARY_EMAIL = "student@example.com";
    private static final String REVOKE_EMAIL = "student-revoke@example.com";
    private static final String PROFILE_EMAIL = "student-profile@example.com";
    private static final String EVENTS_EMAIL = "student-events@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void emailAuthFlowCreatesSessionAndCurrentUser() throws Exception {
        MvcResult tokenResult = mockMvc.perform(post("/api/v1/auth/email-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "purpose": "login"
                                }
                                """.formatted(PRIMARY_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("student@example.com"))
                .andExpect(jsonPath("$.data.purpose").value("login"))
                .andExpect(jsonPath("$.data.delivery").value("debug_return"))
                .andExpect(jsonPath("$.data.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())))
                .andExpect(jsonPath("$.durationMs", greaterThanOrEqualTo(0)))
                .andReturn();

        String token = readJson(tokenResult).at("/data/token").asText();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/email-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "purpose": "login",
                                  "token": "%s",
                                  "deviceLabel": "Chrome on Windows"
                                }
                                """.formatted(PRIMARY_EMAIL, token)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XJTUHUB_SESSION"))
                .andExpect(jsonPath("$.data.user.nickname").value("student"))
                .andExpect(jsonPath("$.data.user.authLevel").value("email_user"))
                .andExpect(jsonPath("$.data.user.nameColor").value("default"))
                .andExpect(jsonPath("$.data.user.displayBadges", hasSize(1)))
                .andExpect(jsonPath("$.data.user.displayBadges[0].code").value("email_verified"))
                .andExpect(jsonPath("$.data.session.loginProvider").value("email"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())))
                .andExpect(jsonPath("$.durationMs", greaterThanOrEqualTo(0)))
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie("XJTUHUB_SESSION");

        mockMvc.perform(get("/api/v1/users/me").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("student"))
                .andExpect(jsonPath("$.data.displayBadges[0].code").value("email_verified"))
                .andExpect(jsonPath("$.data.lastLoginProvider").value("email"))
                .andExpect(jsonPath("$.data.identitySummary").value("邮箱已验证"))
                .andExpect(jsonPath("$.data.identityBindings", hasSize(1)))
                .andExpect(jsonPath("$.data.identityBindings[0].provider").value("email"))
                .andExpect(jsonPath("$.data.identityBindings[0].verificationStatus").value("verified"))
                .andExpect(jsonPath("$.data.identityBindings[0].primary").value(true));

        mockMvc.perform(get("/api/v1/auth/sessions").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].current").value(true))
                .andExpect(jsonPath("$.data.items[0].loginProvider").value("email"))
                .andExpect(jsonPath("$.data.items[0].deviceLabel").value("Chrome on Windows"));

        mockMvc.perform(delete("/api/v1/auth/sessions/current").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("XJTUHUB_SESSION", 0))
                .andExpect(jsonPath("$.data.revoked").value(true));

        mockMvc.perform(get("/api/v1/users/me").cookie(sessionCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_LOGIN_REQUIRED"));
    }

    @Test
    void invalidEmailTokenReturnsStableErrorCode() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "purpose": "login",
                                  "token": "bad-token"
                                }
                                """.formatted(PRIMARY_EMAIL)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_TOKEN_INVALID"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())))
                .andExpect(jsonPath("$.durationMs", greaterThanOrEqualTo(0)));
    }

    @Test
    void revokeSessionByIdKeepsCurrentSessionActive() throws Exception {
        Cookie firstSession = loginAndGetSessionCookie(REVOKE_EMAIL, "device-one");
        String firstSessionId = currentSessionId(firstSession);

        Cookie secondSession = loginAndGetSessionCookie(REVOKE_EMAIL, "device-two");
        String secondSessionId = currentSessionId(secondSession);

        mockMvc.perform(delete("/api/v1/auth/sessions/{sessionId}", firstSessionId).cookie(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revoked").value(true));

        mockMvc.perform(get("/api/v1/auth/sessions").cookie(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id").value(secondSessionId))
                .andExpect(jsonPath("$.data.items[0].current").value(true));

        mockMvc.perform(get("/api/v1/users/me").cookie(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("student-revoke"));

        mockMvc.perform(get("/api/v1/users/me").cookie(firstSession))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_LOGIN_REQUIRED"));
    }

    @Test
    void currentUserRequestRefreshesSessionLastSeenTime() throws Exception {
        Cookie sessionCookie = loginAndGetSessionCookie("student-lastseen@example.com", "last-seen-device");

        MvcResult beforeResult = mockMvc.perform(get("/api/v1/auth/sessions").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].lastSeenAt").exists())
                .andReturn();
        String beforeLastSeenAt = readJson(beforeResult).at("/data/items/0/lastSeenAt").asText();

        Thread.sleep(5L);

        mockMvc.perform(get("/api/v1/users/me").cookie(sessionCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auth/sessions").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].lastSeenAt", not(blankOrNullString())))
                .andExpect(jsonPath("$.data.items[0].lastSeenAt").value(not(beforeLastSeenAt)));
    }

    @Test
    void updateCurrentUserProfileReturnsUpdatedUser() throws Exception {
        Cookie sessionCookie = loginAndGetSessionCookie(PROFILE_EMAIL, "profile-device");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/users/me")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "Profile User",
                                  "bio": "Updated profile bio",
                                  "avatarUrl": "https://example.com/avatar.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Profile User"))
                .andExpect(jsonPath("$.data.bio").value("Updated profile bio"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://example.com/avatar.png"));

        mockMvc.perform(get("/api/v1/users/me").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Profile User"))
                .andExpect(jsonPath("$.data.bio").value("Updated profile bio"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://example.com/avatar.png"));
    }

    @Test
    void updateCurrentUserProfileRejectsTrimmedBlankNicknameAndInvalidAvatarUrl() throws Exception {
        Cookie sessionCookie = loginAndGetSessionCookie("student-profile-invalid@example.com", "profile-invalid-device");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/users/me")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "   ",
                                  "bio": "ok",
                                  "avatarUrl": "javascript:alert(1)"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.details.fields.nickname").exists())
                .andExpect(jsonPath("$.error.details.fields.avatarUrl").exists());
    }

    @Test
    void loginEventsEndpointReturnsSafeHistory() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "purpose": "login",
                                  "token": "bad-token"
                                }
                                """.formatted(EVENTS_EMAIL)))
                .andExpect(status().isUnauthorized());

        Cookie sessionCookie = loginAndGetSessionCookie(EVENTS_EMAIL, "events-device");

        mockMvc.perform(get("/api/v1/auth/login-events").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[*].provider", hasItem("email")))
                .andExpect(jsonPath("$.data.items[*].eventType", hasItem("email_token_login")))
                .andExpect(jsonPath("$.data.items[*].success", hasItem(true)))
                .andExpect(jsonPath("$.data.items[*].success", hasItem(false)))
                .andExpect(jsonPath("$.data.items[*].failureReason", hasItem("token_invalid")))
                .andExpect(jsonPath("$.data.items[0].ipAddress").doesNotExist())
                .andExpect(jsonPath("$.data.items.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void campusScanReservedEndpointsReturnStableNotImplementedError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/campus-scan/sessions"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.error.code").value("AUTH_CAMPUS_SCAN_RESERVED"));

        mockMvc.perform(get("/api/v1/auth/campus-scan/sessions/{sceneId}", "scene-test"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.error.code").value("AUTH_CAMPUS_SCAN_RESERVED"));

        mockMvc.perform(post("/api/v1/auth/campus-scan/sessions/{sceneId}/confirm", "scene-test"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.error.code").value("AUTH_CAMPUS_SCAN_RESERVED"));
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
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

    private String currentSessionId(Cookie sessionCookie) throws Exception {
        MvcResult sessions = mockMvc.perform(get("/api/v1/auth/sessions").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(sessions).at("/data/items/0/id").asText();
    }
}
