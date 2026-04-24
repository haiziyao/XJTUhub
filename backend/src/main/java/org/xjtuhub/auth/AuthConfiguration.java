package org.xjtuhub.auth;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.xjtuhub.auth.persistence.mapper.CampusAppLoginSessionMapper;
import org.xjtuhub.auth.persistence.mapper.EmailVerificationTokenMapper;
import org.xjtuhub.auth.persistence.mapper.SessionMapper;
import org.xjtuhub.auth.persistence.mapper.UserAuthIdentityMapper;
import org.xjtuhub.auth.persistence.mapper.UserLoginEventMapper;
import org.xjtuhub.auth.persistence.mapper.UserMapper;
import org.xjtuhub.auth.persistence.mapper.UserMembershipMapper;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthConfiguration {
    @Bean
    AuthStore authStore(
            ObjectProvider<SqlSessionFactory> sqlSessionFactoryProvider,
            ObjectProvider<EmailVerificationTokenMapper> tokenMapperProvider,
            ObjectProvider<UserMapper> userMapperProvider,
            ObjectProvider<UserAuthIdentityMapper> userAuthIdentityMapperProvider,
            ObjectProvider<SessionMapper> sessionMapperProvider,
            ObjectProvider<UserLoginEventMapper> userLoginEventMapperProvider,
            ObjectProvider<UserMembershipMapper> userMembershipMapperProvider,
            org.xjtuhub.common.support.IdGenerator idGenerator,
            InMemoryAuthStore inMemoryAuthStore
    ) {
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryProvider.getIfAvailable();
        if (sqlSessionFactory != null) {
            return new MybatisAuthStore(
                    require(tokenMapperProvider, EmailVerificationTokenMapper.class),
                    require(userMapperProvider, UserMapper.class),
                    require(userAuthIdentityMapperProvider, UserAuthIdentityMapper.class),
                    require(sessionMapperProvider, SessionMapper.class),
                    require(userLoginEventMapperProvider, UserLoginEventMapper.class),
                    require(userMembershipMapperProvider, UserMembershipMapper.class),
                    idGenerator
            );
        }
        return inMemoryAuthStore;
    }

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
    CampusScanStore campusScanStore(
            ObjectProvider<SqlSessionFactory> sqlSessionFactoryProvider,
            ObjectProvider<CampusAppLoginSessionMapper> campusAppLoginSessionMapperProvider,
            org.xjtuhub.common.support.IdGenerator idGenerator,
            InMemoryCampusScanStore inMemoryCampusScanStore
    ) {
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryProvider.getIfAvailable();
        CampusAppLoginSessionMapper mapper = campusAppLoginSessionMapperProvider.getIfAvailable();
        if (sqlSessionFactory != null && mapper != null) {
            return new MybatisCampusScanStore(mapper, idGenerator);
        }
        return inMemoryCampusScanStore;
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

    private <T> T require(ObjectProvider<T> provider, Class<T> type) {
        T bean = provider.getIfAvailable();
        if (bean == null) {
            throw new IllegalStateException(type.getSimpleName() + " is required when SqlSessionFactory is available.");
        }
        return bean;
    }
}
