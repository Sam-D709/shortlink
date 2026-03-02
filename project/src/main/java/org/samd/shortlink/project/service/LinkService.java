package org.samd.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.samd.shortlink.project.dao.entity.LinkDO;
import org.samd.shortlink.project.dto.req.LinkCreateReqDTO;
import org.samd.shortlink.project.dto.req.LinkUpdateBaseReqDTO;
import org.samd.shortlink.project.dto.req.LinkUpdateGidReqDTO;
import org.samd.shortlink.project.dto.resp.LinkGroupCountQueryRespDTO;
import org.samd.shortlink.project.dto.resp.LinkRespDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface LinkService extends IService<LinkDO> {

    /**
     * 创建短链接
     *
     * @param requestParam 请求参数
     * @return 返回参数
     */
    LinkRespDTO createLink(LinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     *
     * @return 返回参数
     */
    IPage<LinkRespDTO> getPageLink(String gid,long current,long size);

    /**
     * 查询分组下短链接数量
     *
     * @param requestParam 请求参数
     * @return 返回参数
     */
    List<LinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 修改短链接基本数据
     *
     * @param requestParam 请求参数
     * @return 返回参数
     */
    Boolean updateLinkBase(LinkUpdateBaseReqDTO requestParam);

    /**
     * 修改短链接分组
     *
     * @param requestParam 请求参数
     * @return 返回参数
     */
    Boolean updateLinkGid(LinkUpdateGidReqDTO requestParam);

    /**
     * 短链接跳转原始链接
     *
     * @param shortlink 短链接
     * @return 返回参数
     */
    ResponseEntity<Void> link2Orginurl(String shortlink);
}
