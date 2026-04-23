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
                .andExpect(jsonPath("$.data.lastLoginProvider").value("email"));

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
