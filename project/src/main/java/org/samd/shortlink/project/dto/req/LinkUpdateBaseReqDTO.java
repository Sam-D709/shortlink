package org.samd.shortlink.project.dto.req;

import lombok.Data;

@Data
public class LinkUpdateBaseReqDTO {
    /**
     * 短链接id
     */
    private String id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 原始链接
     */
    private String originurl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 创建类型 0：接口创建 1：控制台创建
     */
    private Integer createdtype;

    /**
     * 有效期类型 0：永久有效 1：自定义
     */
    private Integer validdatetype;

    /**
     * 有效期
     */
    private Integer validdate;

    /**
     * 描述
     */
    private String description;
}
