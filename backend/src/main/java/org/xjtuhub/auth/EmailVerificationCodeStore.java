package org.xjtuhub.auth;

import java.time.Duration;

interface EmailVerificationCodeStore {
    void save(String email, String purpose, String tokenHash, Duration ttl);

    String getTokenHash(String email, String purpose);

    void delete(String email, String purpose);
}
