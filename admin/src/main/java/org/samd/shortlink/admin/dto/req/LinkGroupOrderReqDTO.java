package org.samd.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class LinkGroupOrderReqDTO {

    /**
     * 分组ID
     */
    private String gid;

    /**
     * 排序
     */
    private Integer sortOrder;
}
