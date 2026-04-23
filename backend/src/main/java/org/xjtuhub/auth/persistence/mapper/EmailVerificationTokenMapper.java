package org.xjtuhub.auth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xjtuhub.auth.persistence.entity.EmailVerificationTokenEntity;

@Mapper
public interface EmailVerificationTokenMapper extends BaseMapper<EmailVerificationTokenEntity> {
}
