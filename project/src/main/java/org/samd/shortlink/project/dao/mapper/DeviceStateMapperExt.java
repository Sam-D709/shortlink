package org.samd.shortlink.project.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.samd.shortlink.project.dao.entity.DeviceStateMonthDO;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DeviceStateMapperExt {
    @Select("SELECT fullshorturl, device, YEAR(date) as year, MONTH(date) as month, SUM(cnt) as cnt FROM devicestate WHERE date >= #{start} AND date < #{end} AND delflag = 0 GROUP BY fullshorturl, device, YEAR(date), MONTH(date)")
    List<DeviceStateMonthDO> selectMonthStat(@Param("start") LocalDate start, @Param("end") LocalDate end);
}

