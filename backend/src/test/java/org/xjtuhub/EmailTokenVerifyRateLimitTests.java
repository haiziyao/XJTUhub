package org.xjtuhub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "xjtuhub.auth.email.debug-return-token=true",
        "xjtuhub.auth.email.token-verify-limit=2",
        "xjtuhub.auth.email.token-verify-window-seconds=3600"
})
@AutoConfigureMockMvc
class EmailTokenVerifyRateLimitTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void emailTokenVerificationIsRateLimitedWithinWindow() throws Exception {
        String payload = """
                {
                  "email": "verify-limited@example.com",
                  "purpose": "login",
                  "token": "bad-token"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/email-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_TOKEN_INVALID"));

        mockMvc.perform(post("/api/v1/auth/email-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_EMAIL_TOKEN_INVALID"));

        mockMvc.perform(post("/api/v1/auth/email-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())))
                .andExpect(jsonPath("$.durationMs", greaterThanOrEqualTo(0)));
    }
}
