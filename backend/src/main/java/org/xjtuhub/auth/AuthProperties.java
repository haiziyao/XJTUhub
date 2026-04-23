package org.xjtuhub.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xjtuhub.auth")
public class AuthProperties {
    private final Email email = new Email();
    private final Session session = new Session();

    public Email getEmail() {
        return email;
    }

    public Session getSession() {
        return session;
    }

    public static class Email {
        private int tokenTtlMinutes = 10;
        private boolean debugReturnToken = false;

        public int getTokenTtlMinutes() {
            return tokenTtlMinutes;
        }

        public void setTokenTtlMinutes(int tokenTtlMinutes) {
            this.tokenTtlMinutes = tokenTtlMinutes;
        }

        public boolean isDebugReturnToken() {
            return debugReturnToken;
        }

        public void setDebugReturnToken(boolean debugReturnToken) {
            this.debugReturnToken = debugReturnToken;
        }
    }

    public static class Session {
        private String cookieName = "XJTUHUB_SESSION";
        private int ttlDays = 30;

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

        public int getTtlDays() {
            return ttlDays;
        }

        public void setTtlDays(int ttlDays) {
            this.ttlDays = ttlDays;
        }
    }
}
