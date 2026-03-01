package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.BrowserStateDO;

@Mapper
public interface BrowserStateMapper extends BaseMapper<BrowserStateDO> {
    @Insert("INSERT INTO " +
            "browserstate (fullshorturl, date, delflag, cnt, browser)" +
            "VALUES( #{browserState.fullshorturl}, #{browserState.date}, 0, #{browserState.cnt}, #{browserState.browser}) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt + #{browserState.cnt};")
    void shortLinkBrowserState(@Param("browserState") BrowserStateDO browserStatsDO);
}
