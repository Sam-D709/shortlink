package org.samd.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.samd.shortlink.admin.common.biz.user.UserContext;
import org.samd.shortlink.admin.common.util.ParamValidator;
import org.samd.shortlink.admin.common.util.RandomCodeUtil;
import org.samd.shortlink.admin.dao.entity.GroupDO;
import org.samd.shortlink.admin.dao.mapper.GroupMapper;
import org.samd.shortlink.admin.dto.req.LinkGroupOrderReqDTO;
import org.samd.shortlink.admin.dto.resp.GroupRespDTO;
import org.samd.shortlink.admin.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService{

    @Override
    public Boolean createGroup(String groupName) {
        String gid;
        do{
            gid = RandomCodeUtil.generate();
        }while(hasGid(gid)>0);
        GroupDO groupDO = new GroupDO();
        groupDO.setGid(gid);
        groupDO.setName(groupName);
        groupDO.setSortorder(0);
        groupDO.setUsername(UserContext.getUsername());
        return save(groupDO);
    }

    @Override
    public List<GroupRespDTO> listGroups() {
        QueryWrapper<GroupDO> qw = new QueryWrapper<>();
        log.info("当前用户：{}",UserContext.getUsername());
        qw.eq("username",UserContext.getUsername())
                .eq("delflag",0)
                .orderByDesc("sortorder","updatetime");
        List<GroupDO> groupDOList = list(qw);
        ParamValidator.objNonNull(groupDOList);
        return BeanUtil.copyToList(groupDOList,GroupRespDTO.class);
    }

    @Override
    public Boolean updateGroupName(String gid, String groupName) {
        UpdateWrapper<GroupDO> uw = new UpdateWrapper<>();
        uw.eq("gid",gid)
                .eq("username",UserContext.getUsername())
                .eq("delflag",0)
                .set("name",groupName);
        return update(uw);
    }

    @Override
    public Boolean deleteGroup(String gid) {
        UpdateWrapper<GroupDO> uw = new UpdateWrapper<>();
        uw.eq("gid",gid)
                .eq("username",UserContext.getUsername())
                .eq("delflag",0)
                .set("delflag",1);
        return update(uw);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateSortOrder(List<LinkGroupOrderReqDTO> orders) {
        for(LinkGroupOrderReqDTO order:orders){
            UpdateWrapper<GroupDO> uw = new UpdateWrapper<>();
            uw.eq("gid",order.getGid())
                    .eq("username",UserContext.getUsername())
                    .eq("delflag",0)
                    .set("sortorder",order.getSortOrder());
            update(uw);
        }
        return true;
    }

    Long hasGid(String gid) {
        QueryWrapper<GroupDO> qw = new QueryWrapper<>();
        qw.eq("gid", gid)
                .eq("username",UserContext.getUsername())
                .eq("delflag",0);
        return count(qw);
    }
}