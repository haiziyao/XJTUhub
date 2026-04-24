package org.xjtuhub.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "xjtuhub.auth")
public class AuthProperties {
    private final Email email = new Email();
    private final Session session = new Session();

    @Getter
    @Setter
    public static class Email {
        private int tokenTtlMinutes = 10;
        private boolean debugReturnToken = false;
        private int tokenCreateLimit = 5;
        private int tokenCreateWindowSeconds = 3600;
        private int tokenVerifyLimit = 10;
        private int tokenVerifyWindowSeconds = 900;
        private String fromAddress;
        private String fromName = "XJTUhub";
    }

    @Getter
    @Setter
    public static class Session {
        private String cookieName = "XJTUHUB_SESSION";
        private int ttlDays = 30;
    }
}
