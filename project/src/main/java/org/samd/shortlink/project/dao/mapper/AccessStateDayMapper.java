package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.AccessStateDayDO;

import java.util.List;

@Mapper
public interface AccessStateDayMapper extends BaseMapper<AccessStateDayDO> {
    void batchInsertOrUpdate(@Param("list") List<AccessStateDayDO> list);
}