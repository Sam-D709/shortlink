package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.OSStateDO;

import java.util.List;

@Mapper
public interface OSStateMapper extends BaseMapper<OSStateDO> {
    void batchInsertOrUpdate(@Param("list") List<OSStateDO> list);
}
