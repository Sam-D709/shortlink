package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.BrowserStatsDO;

@Mapper
public interface BrowserStatsMapper extends BaseMapper<BrowserStatsDO> {
    @Insert("INSERT INTO " +
            "browserstats (fullshorturl, date, delflag, cnt, browser)" +
            "VALUES( #{browserstats.fullshorturl}, #{browserstats.date}, 0, #{browserstats.cnt}, #{browserstats.browser}) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{browserstats.cnt};")
    void shortLinkBrowserStats(@Param("browserstats") BrowserStatsDO browserStatsDO);
}
