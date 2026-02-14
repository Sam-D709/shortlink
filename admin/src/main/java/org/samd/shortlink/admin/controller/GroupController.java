package org.samd.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.samd.shortlink.admin.common.conversion.result.Result;
import org.samd.shortlink.admin.common.conversion.result.Results;
import org.samd.shortlink.admin.dto.req.LinkGroupOrderReqDTO;
import org.samd.shortlink.admin.dto.resp.GroupRespDTO;
import org.samd.shortlink.admin.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 保存分组
     * @param groupName 分组名称
     * @return 保存结果
     */
    @PostMapping("/api/shortlink/admin/group/create")
    public Result<Boolean> saveGroup(@RequestParam String groupName) {
        return Results.success(groupService.createGroup(groupName));
    }

    /**
     * 获取分组列表
     * @return 分组列表
     */
    @GetMapping("/api/shortlink/admin/group/list")
    public Result<List<GroupRespDTO>> listGroups(){
        return Results.success(groupService.listGroups());
    }

    /**
     * 更新分组名称
     * @param gid 分组ID
     * @param groupName 分组名称
     * @return 更新结果
     */
    @PutMapping("/api/shortlink/admin/group/updatename")
    public Result<Boolean> updateGroupName(@RequestParam String gid, @RequestParam String groupName){
        return Results.success(groupService.updateGroupName(gid,groupName));
    }

    /**
     * 删除分组
     * @param gid 分组ID
     * @return 删除结果
     */
    @DeleteMapping("/api/shortlink/admin/group/delete")
    public Result<Boolean> deleteGroup(@RequestParam String gid){
        return Results.success(groupService.deleteGroup(gid));
    }

    /**
     * 分组排序
     * @param requestParam 排序请求参数
     * @return 排序结果
     */
    @PostMapping("/api/shortlink/admin/group/order")
    public Result<Boolean> orderGroups(@RequestBody List<LinkGroupOrderReqDTO> requestParam){
        return Results.success(groupService.updateSortOrder(requestParam));
    }
}
