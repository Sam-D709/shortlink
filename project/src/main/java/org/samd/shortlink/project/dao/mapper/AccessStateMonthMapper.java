package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.AccessStateMonthDO;

import java.util.List;

@Mapper
public interface AccessStateMonthMapper extends BaseMapper<AccessStateMonthDO>{
    void batchInsertOrUpdate(@Param("list") List<AccessStateMonthDO> list);
}
