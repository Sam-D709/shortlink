package org.samd.shortlink.project.dto.req;

import lombok.Data;

@Data
public class LinkUpdateGidReqDTO {
    /**
     * 短链接id
     */
    private String id;

    /**
     * 旧分组标识
     */
    private String oldGid;

    /**
     * 新分组标识
     */
    private String newGid;
}
