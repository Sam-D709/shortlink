package org.samd.shortlink.project.dto.req;

import lombok.Data;

@Data
public class LinkDayStateReqDTO {
    /**
     * 完整短链接
     */
    private String fullshorturl;
    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;
}
