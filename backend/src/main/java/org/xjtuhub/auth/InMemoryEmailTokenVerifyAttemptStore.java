package org.xjtuhub.auth;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryEmailTokenVerifyAttemptStore implements EmailTokenVerifyAttemptStore {
    private final Map<String, Deque<Instant>> failuresByKey = new ConcurrentHashMap<>();

    @Override
    public int countRecentFailures(String email, String purpose, Instant windowStart) {
        Deque<Instant> attempts = failuresByKey.get(key(email, purpose));
        if (attempts == null) {
            return 0;
        }
        synchronized (attempts) {
            trim(attempts, windowStart);
            return attempts.size();
        }
    }

    @Override
    public void recordFailure(String email, String purpose, Instant now) {
        Deque<Instant> attempts = failuresByKey.computeIfAbsent(key(email, purpose), ignored -> new ArrayDeque<>());
        synchronized (attempts) {
            attempts.addLast(now);
        }
    }

    @Override
    public void clearFailures(String email, String purpose) {
        failuresByKey.remove(key(email, purpose));
    }

    private void trim(Deque<Instant> attempts, Instant windowStart) {
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(windowStart)) {
            attempts.removeFirst();
        }
    }

    private String key(String email, String purpose) {
        return email + "|" + purpose;
    }
}
