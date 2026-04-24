package org.xjtuhub.auth;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisEmailVerificationCodeStoreTests {

    @Test
    void saveUsesExpectedKeyPrefixAndFiveMinuteTtl() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        RedisEmailVerificationCodeStore store = new RedisEmailVerificationCodeStore(stringRedisTemplate);
        store.save("student@example.com", "login", "hash-value", Duration.ofMinutes(5));

        verify(valueOperations).set(
                "xjtuhub:auth:email-code:login:student@example.com",
                "hash-value",
                Duration.ofMinutes(5)
        );
    }
}
