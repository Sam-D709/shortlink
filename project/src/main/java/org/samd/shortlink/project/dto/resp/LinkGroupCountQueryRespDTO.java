package org.samd.shortlink.project.dto.resp;

import lombok.Data;

@Data
public class LinkGroupCountQueryRespDTO {
    /**
     * 分组id
     */
    private String gid;

    /**
     * 短链接数量
     */
    private Integer linkCount;
}
