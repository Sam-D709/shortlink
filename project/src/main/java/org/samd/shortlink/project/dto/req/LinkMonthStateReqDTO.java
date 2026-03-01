package org.samd.shortlink.project.dto.req;

import lombok.Data;

@Data
public class LinkMonthStateReqDTO {
    /**
     * 完整短链接
     */
    private String fullshorturl;
    /**
     * 开始月份 yyyy-MM
     */
    private String startMonth;
    /**
     * 结束月份 yyyy-MM
     */
    private String endMonth;
    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;
}
