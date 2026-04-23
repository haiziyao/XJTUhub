package org.xjtuhub;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.xjtuhub.auth.EmailSender;
import org.xjtuhub.auth.EmailSender.EmailMessage;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "xjtuhub.auth.email.debug-return-token=false",
        "xjtuhub.auth.email.token-create-limit=1",
        "xjtuhub.auth.email.token-create-window-seconds=3600"
})
@AutoConfigureMockMvc
class EmailTokenRateLimitTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailSender emailSender;

    @Test
    void emailTokenRequestUsesSenderWhenDebugReturnDisabled() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "sender@example.com",
                                  "purpose": "login"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.delivery").value("accepted"))
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())))
                .andExpect(jsonPath("$.durationMs", greaterThanOrEqualTo(0)));

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, times(1)).send(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().to()).isEqualTo("sender@example.com");
    }

    @Test
    void emailTokenRequestIsRateLimitedWithinWindow() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "limited@example.com",
                                  "purpose": "login"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/email-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "limited@example.com",
                                  "purpose": "login"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.requestId", not(blankOrNullString())))
                .andExpect(jsonPath("$.durationMs", greaterThanOrEqualTo(0)));

        verify(emailSender, times(1)).send(org.mockito.ArgumentMatchers.any());
    }
}
