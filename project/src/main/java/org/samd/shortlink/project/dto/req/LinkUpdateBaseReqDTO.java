package org.samd.shortlink.project.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LinkUpdateBaseReqDTO {
    /**
     * 短链接id
     */
    private String id;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validdate;

    /**
     * 描述
     */
    private String description;
}
