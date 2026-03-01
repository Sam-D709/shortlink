package org.samd.shortlink.project.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.samd.shortlink.project.dao.entity.BrowserStateMonthDO;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BrowserStateMapperExt {
    @Select("SELECT fullshorturl, browser, YEAR(date) as year, MONTH(date) as month, SUM(cnt) as cnt FROM browserstate WHERE date >= #{start} AND date < #{end} AND delflag = 0 GROUP BY fullshorturl, browser, YEAR(date), MONTH(date)")
    List<BrowserStateMonthDO> selectMonthStat(@Param("start") LocalDate start, @Param("end") LocalDate end);
}

