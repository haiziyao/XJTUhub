package org.xjtuhub.auth;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.xjtuhub.auth.persistence.mapper.CampusAppLoginSessionMapper;
import org.xjtuhub.auth.persistence.mapper.EmailVerificationTokenMapper;
import org.xjtuhub.auth.persistence.mapper.SessionMapper;
import org.xjtuhub.auth.persistence.mapper.UserAuthIdentityMapper;
import org.xjtuhub.auth.persistence.mapper.UserLoginEventMapper;
import org.xjtuhub.auth.persistence.mapper.UserMapper;
import org.xjtuhub.auth.persistence.mapper.UserMembershipMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Import(CampusScanStoreSelectionTests.MybatisStoreTestConfiguration.class)
class CampusScanStoreSelectionTests {
    @Autowired
    private CampusScanStore campusScanStore;

    @Test
    void mybatisCampusScanStoreIsPreferredWhenSqlSessionFactoryExists() {
        assertThat(campusScanStore.getClass().getSimpleName()).isEqualTo("MybatisCampusScanStore");
    }

    @TestConfiguration
    static class MybatisStoreTestConfiguration {
        @Bean
        SqlSessionFactory sqlSessionFactory() {
            return mock(SqlSessionFactory.class);
        }

        @Bean
        CampusAppLoginSessionMapper campusAppLoginSessionMapper() {
            return mock(CampusAppLoginSessionMapper.class);
        }

        @Bean
        EmailVerificationTokenMapper emailVerificationTokenMapper() {
            return mock(EmailVerificationTokenMapper.class);
        }

        @Bean
        UserMapper userMapper() {
            return mock(UserMapper.class);
        }

        @Bean
        UserAuthIdentityMapper userAuthIdentityMapper() {
            return mock(UserAuthIdentityMapper.class);
        }

        @Bean
        SessionMapper sessionMapper() {
            return mock(SessionMapper.class);
        }

        @Bean
        UserLoginEventMapper userLoginEventMapper() {
            return mock(UserLoginEventMapper.class);
        }

        @Bean
        UserMembershipMapper userMembershipMapper() {
            return mock(UserMembershipMapper.class);
        }
    }
}
