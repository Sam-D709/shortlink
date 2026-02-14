package org.samd.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("shortlink2gid")
public class Shortlink2GidDO {
    @TableId(type = IdType.AUTO)
    /*
      ID
     */
    private Long id;
    /**
     * 完整短链接
     */
    private String fullshorturl;
    /**
     * 分组标识
     */
    private String gid;
    /**
     * 注销标识，1:已经删除
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delflag;
}
