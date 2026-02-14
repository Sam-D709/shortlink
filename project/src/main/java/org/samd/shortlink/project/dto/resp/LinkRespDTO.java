package org.samd.shortlink.project.dto.resp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LinkRespDTO {
    @TableId(type = IdType.AUTO)
    /*
      ID
     */
    private String id;

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

    /**
     * 创建时间
     */
    private LocalDateTime createtime;

    /**
     * 修改时间
     */
    private LocalDateTime updatetime;
}
