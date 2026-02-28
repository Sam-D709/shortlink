package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.AccessStatsDO;

@Mapper
public interface AccessStatsMapper extends BaseMapper<AccessStatsDO> {

    @Insert("INSERT INTO " +
            "accessstats (fullshorturl, date, pv, uv, uip, hour, weekday, delflag)" +
            "VALUES( #{accessStats.fullshorturl}, #{accessStats.date}, #{accessStats.pv}, #{accessStats.uv}, #{accessStats.uip}, #{accessStats.hour}, #{accessStats.weekday}, 0) " +
            "ON DUPLICATE KEY UPDATE pv = pv +  #{accessStats.pv}, uv = uv + #{accessStats.uv}, uip = uip + #{accessStats.uip};")
    void shortLinkStats(@Param("accessStats") AccessStatsDO accessStatsDO);
}
