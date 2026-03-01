package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.AccessStateDayDO;

@Mapper
public interface AccessStateDayMapper extends BaseMapper<AccessStateDayDO> {
    @Insert("INSERT INTO " +
            "accessstateday (fullshorturl, date, pv, uv, delflag)" +
            "VALUES( #{accessStateDay.fullshorturl}, #{accessStateDay.date}, #{accessStateDay.pv}, #{accessStateDay.uv}, 0) " +
            "ON DUPLICATE KEY UPDATE pv = pv +  #{accessStateDay.pv}, uv = uv + #{accessStateDay.uv};")
    void shortLinkState(@Param("accessStateDay") AccessStateDayDO accessStateDay);
}