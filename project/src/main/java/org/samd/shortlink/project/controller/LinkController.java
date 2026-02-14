package org.samd.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.samd.shortlink.project.common.conversion.result.Result;
import org.samd.shortlink.project.common.conversion.result.Results;
import org.samd.shortlink.project.dto.req.LinkCreateReqDTO;
import org.samd.shortlink.project.dto.req.LinkPageReqDTO;
import org.samd.shortlink.project.dto.req.LinkUpdateBaseReqDTO;
import org.samd.shortlink.project.dto.req.LinkUpdateGidReqDTO;
import org.samd.shortlink.project.dto.resp.LinkGroupCountQueryRespDTO;
import org.samd.shortlink.project.dto.resp.LinkRespDTO;
import org.samd.shortlink.project.service.LinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 创建短链接返回参数
     */
    @PostMapping("/api/shortlink/project/link/create")
    public Result<LinkRespDTO> createLink(@RequestBody LinkCreateReqDTO requestParam) {
        return Results.success(linkService.createLink(requestParam));
    }

    /**
     * 获取短链接分页列表
     *
     * @param requestParam 短链接分页请求参数
     * @return 短链接分页返回参数
     */
    @GetMapping("/api/shortlink/project/getpagelink")
    public Result<IPage<LinkRespDTO>> getPageLink(@RequestBody LinkPageReqDTO requestParam) {
        return Results.success(linkService.getPageLink(requestParam));
    }

    /**
     * 获取分组下短链接数量
     *
     * @param gid 分组标识列表
     * @return 短链接数量返回参数
     */
    @GetMapping("/api/shortlink/project/getlinkcount")
    public Result<List<LinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam List<String> gid) {
        return Results.success(linkService.listGroupShortLinkCount(gid));
    }

    /**
     * 修改短链接基本数据
     *
     * @param requestParam 修改短链接请求参数
     * @return 修改短链接返回参数
     */
    @PutMapping("/api/shortlink/project/link/updatebase")
    public Result<Boolean> updateLink(@RequestBody LinkUpdateBaseReqDTO requestParam) {
        return Results.success(linkService.updateLinkBase(requestParam));
    }

    /**
     * 修改短链接分组
     *
     * @param requestParam 修改短链接分组请求参数
     * @return 修改短链接分组返回参数
     */
    @PutMapping("/api/shortlink/project/link/updategid")
    public Result<Boolean> updateLinkGid(@RequestBody LinkUpdateGidReqDTO requestParam) {
        return Results.success(linkService.updateLinkGid(requestParam));
    }

    /**
     * 短链接还原为原始链接
     *
     * @param shortlink 短链接
     * @return 原始链接
     */
    @GetMapping("/{shortlink}")
    public ResponseEntity<Void> link2Orginurl(@PathVariable String shortlink,HttpServletRequest request) {
        return linkService.link2Orginurl(shortlink,request);
    }
}
