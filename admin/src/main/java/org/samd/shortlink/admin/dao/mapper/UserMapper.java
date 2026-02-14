package org.samd.shortlink.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.samd.shortlink.admin.dao.entity.UserDO;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
