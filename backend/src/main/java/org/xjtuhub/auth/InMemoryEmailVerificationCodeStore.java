package org.xjtuhub.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryEmailVerificationCodeStore implements EmailVerificationCodeStore {
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    @Override
    public void save(String email, String purpose, String tokenHash, Duration ttl) {
        entries.put(key(email, purpose), new Entry(tokenHash, Instant.now().plus(ttl)));
    }

    @Override
    public String getTokenHash(String email, String purpose) {
        Entry entry = entries.get(key(email, purpose));
        if (entry == null) {
            return null;
        }
        if (!entry.expiresAt().isAfter(Instant.now())) {
            entries.remove(key(email, purpose));
            return null;
        }
        return entry.tokenHash();
    }

    @Override
    public void delete(String email, String purpose) {
        entries.remove(key(email, purpose));
    }

    private String key(String email, String purpose) {
        return purpose + ":" + email;
    }

    private record Entry(String tokenHash, Instant expiresAt) {
    }
}
