package org.xjtuhub.admin;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xjtuhub.admin.persistence.mapper.AdminAccountMapper;
import org.xjtuhub.admin.persistence.mapper.AuditLogMapper;
import org.xjtuhub.auth.persistence.mapper.UserAuthIdentityMapper;
import org.xjtuhub.auth.persistence.mapper.UserMapper;
import org.xjtuhub.common.support.IdGenerator;

@Configuration
public class AdminConfiguration {
    @Bean
    AdminStore adminStore(
            ObjectProvider<SqlSessionFactory> sqlSessionFactoryProvider,
            ObjectProvider<AdminAccountMapper> adminAccountMapperProvider,
            ObjectProvider<AuditLogMapper> auditLogMapperProvider,
            ObjectProvider<UserMapper> userMapperProvider,
            ObjectProvider<UserAuthIdentityMapper> userAuthIdentityMapperProvider,
            IdGenerator idGenerator,
            InMemoryAdminStore inMemoryAdminStore
    ) {
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryProvider.getIfAvailable();
        AdminAccountMapper adminAccountMapper = adminAccountMapperProvider.getIfAvailable();
        AuditLogMapper auditLogMapper = auditLogMapperProvider.getIfAvailable();
        UserMapper userMapper = userMapperProvider.getIfAvailable();
        UserAuthIdentityMapper userAuthIdentityMapper = userAuthIdentityMapperProvider.getIfAvailable();
        if (sqlSessionFactory != null
                && adminAccountMapper != null
                && auditLogMapper != null
                && userMapper != null
                && userAuthIdentityMapper != null) {
            return new MybatisAdminStore(
                    adminAccountMapper,
                    auditLogMapper,
                    userMapper,
                    userAuthIdentityMapper,
                    idGenerator
            );
        }
        return inMemoryAdminStore;
    }
}
