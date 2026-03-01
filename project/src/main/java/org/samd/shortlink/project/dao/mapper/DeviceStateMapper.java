package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.DeviceStateDO;

import java.util.List;

@Mapper
public interface DeviceStateMapper extends BaseMapper<DeviceStateDO> {
    void batchInsertOrUpdate(@Param("list") List<DeviceStateDO> list);
}
