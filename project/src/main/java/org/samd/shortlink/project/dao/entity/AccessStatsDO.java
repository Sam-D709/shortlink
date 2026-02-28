package org.samd.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode
@Data
@TableName("accessstats")
public class AccessStatsDO{

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
     * 访问量
     */
    private Integer pv;

    /**
     * 访客数
     */
    private Integer uv;

    /**
     * IP数量
     */
    private Integer uip;

    /**
     * 小时
     */
    private Integer hour;

    /**
     * 星期x
     */
    private Integer weekday;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    private Integer delflag;
}
