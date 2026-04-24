package org.xjtuhub.auth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xjtuhub.auth.persistence.entity.CampusAppLoginSessionEntity;

@Mapper
public interface CampusAppLoginSessionMapper extends BaseMapper<CampusAppLoginSessionEntity> {
}
