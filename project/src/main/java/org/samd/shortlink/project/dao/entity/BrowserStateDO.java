package org.samd.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode
@TableName("browserstate")
public class BrowserStateDO {
    @TableId(type = IdType.AUTO)
    /**
     * ID
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullshorturl;

    /**
     * 日期
     */
    private Date date;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    private Integer delflag;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 浏览器
     */
    private String browser;
}
