package org.samd.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.samd.shortlink.project.common.conversion.result.Result;
import org.samd.shortlink.project.common.conversion.result.Results;
import org.samd.shortlink.project.dao.entity.LinkDO;
import org.samd.shortlink.project.dto.req.RecycleLinkReqDTO;
import org.samd.shortlink.project.dto.resp.LinkRespDTO;
import org.samd.shortlink.project.service.RecycleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RecycleController {

    private final RecycleService recycleService;

    /**
     * 回收站链接创建
     */
    @PutMapping("/api/shortlink/project/recyclelink/create")
    public Result<Boolean> saveRecycleLink(@RequestBody RecycleLinkReqDTO requestParam){
        return Results.success(recycleService.saveRecycleLink(requestParam));
    }

    /**
     * 分页查询回收站链接
     *
     * @return 返回参数
     */
    @GetMapping("/api/shortlink/project/recyclelink/getpage")
    public Result<IPage<LinkRespDTO>> getPageRecycleLink(HttpServletRequest request,@RequestBody Page<LinkDO> requestParam){
        return Results.success(recycleService.getPageRecycleLink(request,requestParam));
    }

    /**
     * 恢复回收站链接
     */
    @PutMapping("/api/shortlink/project/recyclelink/recover")
    public Result<Boolean> recoverLink(@RequestBody RecycleLinkReqDTO requestParam){
        return Results.success(recycleService.recoverLink(requestParam));
    }

    /**
     * 删除回收站链接
     */
    @DeleteMapping("/api/shortlink/project/recyclelink/delete")
    public Result<Boolean> deleteRecycleLink(@RequestBody RecycleLinkReqDTO requestParam){
        return Results.success(recycleService.deleteRecycleLink(requestParam));
    }
}
