package org.samd.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.samd.shortlink.admin.dao.entity.GroupDO;
import org.samd.shortlink.admin.dto.req.LinkGroupOrderReqDTO;
import org.samd.shortlink.admin.dto.resp.GroupRespDTO;

import java.util.List;

public interface GroupService extends IService<GroupDO> {

    /**
     * 保存分组
     * @param groupName 分组名称
     * @return 保存结果
     */
    Boolean createGroup(String groupName);

    /**
     * 获取分组列表
     * @return 分组列表
     */
    List<GroupRespDTO> listGroups();

    /**
     * 更新分组名称
     * @param gid 分组ID
     * @param groupName 分组名称
     * @return 更新结果
     */
    Boolean updateGroupName(String gid, String groupName);

    /**
     * 删除分组
     * @param gid 分组ID
     * @return 删除结果
     */
    Boolean deleteGroup(String gid);

    /**
     * 更新分组排序
     * @param orders 分组排序列表
     * @return 更新结果
     */
    Boolean updateSortOrder(List<LinkGroupOrderReqDTO> orders);
}
