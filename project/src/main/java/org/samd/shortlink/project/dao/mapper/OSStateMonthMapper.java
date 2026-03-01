package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.OSStateMonthDO;

import java.util.List;

@Mapper
public interface OSStateMonthMapper extends BaseMapper<OSStateMonthDO> {
    /**
     * 批量插入月统计
     */
    void batchInsert(@Param("list") List<OSStateMonthDO> list);
}

