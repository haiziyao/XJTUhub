package org.xjtuhub.auth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xjtuhub.auth.persistence.entity.UserEntity;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    @Select("""
            SELECT u.id, u.nickname, u.avatar_url, u.bio, u.account_status, u.auth_level,
                   u.primary_identity_provider, u.last_login_provider, u.created_at, u.updated_at, u.deleted_at
            FROM users u
            JOIN user_auth_identities i ON i.user_id = u.id
            WHERE i.provider = 'email'
              AND i.provider_subject = #{email}
              AND i.deleted_at IS NULL
              AND u.deleted_at IS NULL
            LIMIT 1
            """)
    UserEntity selectByEmail(@Param("email") String email);
}
