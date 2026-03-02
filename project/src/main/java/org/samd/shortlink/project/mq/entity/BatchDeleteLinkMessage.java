package org.samd.shortlink.project.mq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 批量删除短链消息体，包含gid
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDeleteLinkMessage implements Serializable {
    private String gid;
}

