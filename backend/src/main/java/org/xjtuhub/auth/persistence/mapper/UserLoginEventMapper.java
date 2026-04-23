package org.xjtuhub.auth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xjtuhub.auth.persistence.entity.UserLoginEventEntity;

import java.util.List;

@Mapper
public interface UserLoginEventMapper extends BaseMapper<UserLoginEventEntity> {
    @Select("""
            SELECT id, user_id, provider, event_type, success, failure_reason, ip_address, ip_hash, user_agent_hash, created_at
            FROM user_login_events
            WHERE user_id = #{userId} OR user_id IS NULL
            ORDER BY created_at DESC
            LIMIT #{limit}
            """)
    List<UserLoginEventEntity> selectByUserIdOrAnonymous(@Param("userId") long userId, @Param("limit") int limit);
}
