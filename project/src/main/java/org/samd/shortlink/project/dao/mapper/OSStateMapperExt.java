package org.samd.shortlink.project.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.samd.shortlink.project.dao.entity.OSStateMonthDO;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface OSStateMapperExt {
    @Select("SELECT fullshorturl, os, YEAR(date) as year, MONTH(date) as month, SUM(cnt) as cnt FROM osstate WHERE date >= #{start} AND date < #{end} AND delflag = 0 GROUP BY fullshorturl, os, YEAR(date), MONTH(date)")
    List<OSStateMonthDO> selectMonthStat(@Param("start") LocalDate start, @Param("end") LocalDate end);
}

