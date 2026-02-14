package org.samd.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("link")
public class LinkDO extends BaseDO{

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    /*
      ID
     */
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shorturl;

    /**
     * 完整短链接
     */
    private String fullshorturl;

    /**
     * 原始链接
     */
    private String originurl;

    /**
     * 点击量
     */
    private Integer clicknum;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 启用标识 0：未启用 1：已启用
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer enablestatus;

    /**
     * 创建类型 0：控制台 1：接口
     */
    private Integer createdtype;

    /**
     * 有效期类型 0：永久有效 1：用户自定义
     */
    private Integer validdatetype;

    /**
     * 有效期
     */
    private LocalDateTime validdate;

    /**
     * 描述
     */
    private String description;
}