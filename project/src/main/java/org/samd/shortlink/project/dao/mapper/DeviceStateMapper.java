package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.DeviceStateDO;

@Mapper
public interface DeviceStateMapper extends BaseMapper<DeviceStateDO> {
    @Insert("INSERT INTO " +
            "devicestate (fullshorturl, date, delflag, cnt, device)" +
            "VALUES( #{deviceState.fullshorturl}, #{deviceState.date}, 0, #{deviceState.cnt}, #{deviceState.device}) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{deviceState.cnt};")
    void shortLinkDeviceState(@Param("deviceState") DeviceStateDO deviceStatsDO);
}
