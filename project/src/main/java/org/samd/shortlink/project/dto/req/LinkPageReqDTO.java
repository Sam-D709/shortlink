package org.samd.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.samd.shortlink.project.dao.entity.LinkDO;

@EqualsAndHashCode(callSuper = true)
@Data
public class LinkPageReqDTO extends Page<LinkDO> {
    /**
     * 分组标识
     */
    private String gid;
}
