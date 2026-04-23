package org.xjtuhub.auth;

import java.time.Instant;

interface EmailTokenVerifyAttemptStore {
    int countRecentFailures(String email, String purpose, Instant windowStart);

    void recordFailure(String email, String purpose, Instant now);

    void clearFailures(String email, String purpose);
}
