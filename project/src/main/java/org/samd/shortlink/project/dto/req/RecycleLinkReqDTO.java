package org.samd.shortlink.project.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class RecycleLinkReqDTO {
    /**
     * 分组id
     */
    private String gid;

    /**
     * 短链接id列表
     */
    private List<String> id;
}
