package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.DeviceStatsDO;

@Mapper
public interface DeviceStatsMapper extends BaseMapper<DeviceStatsDO> {
    @Insert("INSERT INTO " +
            "devicestats (fullshorturl, date, delflag, cnt, device)" +
            "VALUES( #{deviceStats.fullshorturl}, #{deviceStats.date}, 0, #{deviceStats.cnt}, #{deviceStats.device}) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{deviceStats.cnt};")
    void shortLinkDeviceStats(@Param("deviceStats") DeviceStatsDO deviceStatsDO);
}

