package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.BrowserStateMonthDO;

import java.util.List;

@Mapper
public interface BrowserStateMonthMapper extends BaseMapper<BrowserStateMonthDO> {
    void batchInsert(@Param("list") List<BrowserStateMonthDO> list);
}

