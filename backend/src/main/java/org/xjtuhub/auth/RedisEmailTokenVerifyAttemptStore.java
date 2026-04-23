package org.xjtuhub.auth;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.Instant;

class RedisEmailTokenVerifyAttemptStore implements EmailTokenVerifyAttemptStore {
    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;

    RedisEmailTokenVerifyAttemptStore(StringRedisTemplate stringRedisTemplate, AuthProperties authProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.authProperties = authProperties;
    }

    @Override
    public int countRecentFailures(String email, String purpose, Instant windowStart) {
        String raw = stringRedisTemplate.opsForValue().get(key(email, purpose));
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        return Integer.parseInt(raw);
    }

    @Override
    public void recordFailure(String email, String purpose, Instant now) {
        Long count = stringRedisTemplate.opsForValue().increment(key(email, purpose));
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key(email, purpose), Duration.ofSeconds(authProperties.getEmail().getTokenVerifyWindowSeconds()));
        }
    }

    @Override
    public void clearFailures(String email, String purpose) {
        stringRedisTemplate.delete(key(email, purpose));
    }

    private String key(String email, String purpose) {
        return "xjtuhub:auth:email-token-verify:" + purpose + ":" + email;
    }
}
