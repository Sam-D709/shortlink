package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.OSStateDO;

@Mapper
public interface OSStateMapper extends BaseMapper<OSStateDO> {
    @Insert("INSERT INTO " +
            "osstate (fullshorturl, date, delflag, cnt, os)" +
            "VALUES( #{osstate.fullshorturl}, #{osstate.date}, 0, #{osstate.cnt}, #{osstate.os}) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{osstate.cnt};")
    void shortLinkOSState(@Param("osstate") OSStateDO osStateDO);
}
