package org.samd.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@TableName("osstatemonth")
public class OSStateMonthDO {
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
     * 操作系统
     */
    private String os;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 年
     */
    private String year;
    /**
     * 删除标识 0：未删除 1：已删除
     */
    private Integer delflag;
}
