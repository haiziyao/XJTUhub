package org.xjtuhub.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthConfiguration {
    @Bean
    EmailTokenVerifyAttemptStore emailTokenVerifyAttemptStore(
            ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider,
            AuthProperties authProperties
    ) {
        StringRedisTemplate stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (stringRedisTemplate != null) {
            return new RedisEmailTokenVerifyAttemptStore(stringRedisTemplate, authProperties);
        }
        return new InMemoryEmailTokenVerifyAttemptStore();
    }
}
