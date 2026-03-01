package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.AccessStateMonthDO;

@Mapper
public interface AccessStateMonthMapper extends BaseMapper<AccessStateMonthDO>{
    @Insert("INSERT INTO " +
            "accessstatemonth (fullshorturl, pv, uv, month, year, delflag)" +
            "VALUES( #{accessStateMonth.fullshorturl}, #{accessStateMonth.pv}, #{accessStateMonth.uv}, #{accessStateMonth.month}, #{accessStateMonth.year},0) " +
            "ON DUPLICATE KEY UPDATE pv = pv +  #{accessStateMonth.pv}, uv = uv + #{accessStateMonth.uv};")
    void shortLinkState(@Param("accessStateMonth") AccessStateMonthDO accessStateMonth);
}
