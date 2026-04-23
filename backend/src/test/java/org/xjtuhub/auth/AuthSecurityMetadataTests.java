package org.xjtuhub.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.xjtuhub.common.support.TimeProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "xjtuhub.auth.email.debug-return-token=true"
})
class AuthSecurityMetadataTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthStore authStore;

    @Autowired
    private TimeProvider timeProvider;

    @Test
    void emailSessionStoresSecurityMetadataInSessionStore() throws Exception {
        MockHttpServletRequest tokenRequest = new MockHttpServletRequest();
        tokenRequest.setRemoteAddr("127.0.0.9");
        tokenRequest.addHeader("User-Agent", "JUnit-Agent");

        EmailTokenCreateResponse tokenResponse = authService.createEmailToken(
                new EmailTokenCreateRequest("security@example.com", "login"),
                tokenRequest
        );

        MockHttpServletRequest loginRequest = new MockHttpServletRequest();
        loginRequest.setRemoteAddr("127.0.0.9");
        loginRequest.addHeader("User-Agent", "JUnit-Agent");

        AuthService.LoginResult loginResult = authService.createEmailSession(
                new EmailSessionCreateRequest("security@example.com", "login", tokenResponse.token(), "JUnit Device"),
                loginRequest
        );

        String rawCookie = loginResult.setCookieHeader();
        String rawSessionToken = rawCookie.substring(rawCookie.indexOf('=') + 1, rawCookie.indexOf(';'));
        Instant now = timeProvider.now().plusSeconds(1);

        AuthStore.StoredSession session = authStore.findActiveSession(sha256(rawSessionToken), now).orElseThrow();

        assertThat(session.ipAddress()).isEqualTo("127.0.0.9");
        assertThat(session.ipHash()).isEqualTo(sha256("127.0.0.9"));
        assertThat(session.userAgentHash()).isEqualTo(sha256("JUnit-Agent"));
    }

    private String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
