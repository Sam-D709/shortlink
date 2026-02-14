package org.samd.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.samd.shortlink.project.dao.entity.LinkDO;
import org.samd.shortlink.project.dto.req.RecycleLinkReqDTO;
import org.samd.shortlink.project.dto.resp.LinkRespDTO;

public interface RecycleService extends IService<LinkDO> {

    /**
     * 保存回收站链接
     *
     * @param requestParam 回收站链接请求参数
     * @return 保存结果
     */
    Boolean saveRecycleLink(RecycleLinkReqDTO requestParam);

    /**
     * 分页获取回收站链接
     *
     * @param request      请求参数
     * @param requestParam 分页请求参数
     * @return 回收站链接分页数据
     */
    IPage<LinkRespDTO> getPageRecycleLink(HttpServletRequest request, Page<LinkDO> requestParam);

    /**
     * 恢复回收站链接
     *
     * @param requestParam 恢复回收站链接请求参数
     * @return 恢复结果
     */
    Boolean recoverLink(RecycleLinkReqDTO requestParam);

    /**
     * 删除回收站链接
     *
     * @param requestParam 删除回收站链接请求参数
     * @return 删除结果
     */
    Boolean deleteRecycleLink(RecycleLinkReqDTO requestParam);
}
