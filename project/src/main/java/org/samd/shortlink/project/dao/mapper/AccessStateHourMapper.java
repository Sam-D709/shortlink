package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.AccessStateHourDO;

@Mapper
public interface AccessStateHourMapper extends BaseMapper<AccessStateHourDO> {
    @Insert("INSERT INTO " +
            "accessstatehour (fullshorturl, date, pv, uv, hour, delflag)" +
            "VALUES( #{accessStateHour.fullshorturl}, #{accessStateHour.date}, #{accessStateHour.pv}, #{accessStateHour.uv}, #{accessStateHour.hour}, 0) " +
            "ON DUPLICATE KEY UPDATE pv = pv +  #{accessStateHour.pv}, uv = uv + #{accessStateHour.uv};")
    void shortLinkState(@Param("accessStateHour") AccessStateHourDO accessStateHourDO);
}
