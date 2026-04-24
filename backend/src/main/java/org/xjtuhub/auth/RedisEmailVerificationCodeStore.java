package org.xjtuhub.auth;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

class RedisEmailVerificationCodeStore implements EmailVerificationCodeStore {
    static final String KEY_PREFIX = "xjtuhub:auth:email-code:";

    private final StringRedisTemplate stringRedisTemplate;

    RedisEmailVerificationCodeStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void save(String email, String purpose, String tokenHash, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key(email, purpose), tokenHash, ttl);
    }

    @Override
    public String getTokenHash(String email, String purpose) {
        return stringRedisTemplate.opsForValue().get(key(email, purpose));
    }

    @Override
    public void delete(String email, String purpose) {
        stringRedisTemplate.delete(key(email, purpose));
    }

    private String key(String email, String purpose) {
        return KEY_PREFIX + purpose + ":" + email;
    }
}
