package org.xjtuhub.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;

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

    @Bean
    EmailVerificationCodeStore emailVerificationCodeStore(
            ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider
    ) {
        StringRedisTemplate stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (stringRedisTemplate != null) {
            return new RedisEmailVerificationCodeStore(stringRedisTemplate);
        }
        return new InMemoryEmailVerificationCodeStore();
    }

    @Bean
    EmailSender emailSender(
            ObjectProvider<JavaMailSender> javaMailSenderProvider,
            AuthProperties authProperties
    ) {
        JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();
        if (javaMailSender != null) {
            return new SmtpEmailSender(javaMailSender, authProperties);
        }
        return new LoggingEmailSender();
    }
}
