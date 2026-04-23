package org.xjtuhub.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Import(RedisEmailTokenVerifyAttemptStoreSelectionTests.RedisTemplateTestConfiguration.class)
class RedisEmailTokenVerifyAttemptStoreSelectionTests {

    @Autowired
    private EmailTokenVerifyAttemptStore emailTokenVerifyAttemptStore;

    @Test
    void redisBackedStoreIsPreferredWhenRedisTemplateExists() {
        assertThat(emailTokenVerifyAttemptStore.getClass().getSimpleName()).isEqualTo("RedisEmailTokenVerifyAttemptStore");
    }

    @TestConfiguration
    static class RedisTemplateTestConfiguration {
        @Bean
        StringRedisTemplate stringRedisTemplate() {
            return mock(StringRedisTemplate.class);
        }
    }
}
