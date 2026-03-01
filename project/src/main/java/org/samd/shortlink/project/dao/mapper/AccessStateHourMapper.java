package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.AccessStateHourDO;

import java.util.List;

@Mapper
public interface AccessStateHourMapper extends BaseMapper<AccessStateHourDO> {
    void batchInsertOrUpdate(@Param("list") List<AccessStateHourDO> list);
}