package org.samd.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.samd.shortlink.project.dao.entity.BrowserStateDO;

import java.util.List;

@Mapper
public interface BrowserStateMapper extends BaseMapper<BrowserStateDO> {
    void batchInsertOrUpdate(@Param("list") List<BrowserStateDO> list);
}
