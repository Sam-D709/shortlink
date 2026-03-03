package org.samd.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.samd.shortlink.project.common.util.RedisDelayedDoubleDeleteService;
import org.samd.shortlink.project.common.util.UserContext;
import org.samd.shortlink.project.dao.entity.GroupDO;
import org.samd.shortlink.project.dao.entity.LinkDO;
import org.samd.shortlink.project.dao.entity.Shortlink2GidDO;
import org.samd.shortlink.project.dao.mapper.GroupMapper;
import org.samd.shortlink.project.dao.mapper.LinkMapper;
import org.samd.shortlink.project.dao.mapper.Shortlink2GidMapper;
import org.samd.shortlink.project.dto.req.RecycleLinkReqDTO;
import org.samd.shortlink.project.dto.resp.LinkRespDTO;
import org.samd.shortlink.project.service.RecycleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecycleServiceImpl extends ServiceImpl<LinkMapper,LinkDO> implements RecycleService {

    private final Shortlink2GidMapper shortlink2GidMapper;
    private final GroupMapper groupMapper;
    private final RedisDelayedDoubleDeleteService redisDelayedDoubleDeleteService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRecycleLink(RecycleLinkReqDTO requestParam) {
        if (requestParam == null) {
            log.warn("saveRecycleLink called with null requestParam");
            return false;
        }
        String gid = requestParam.getGid();
        List<String> ids = requestParam.getId();
        if (gid == null || gid.trim().isEmpty() || ids == null || ids.isEmpty()) {
            return false;
        }

        List<LinkDO> linkDOS = list(new QueryWrapper<LinkDO>()
                .eq("gid", gid)
                .in("id", ids)
                .eq("enablestatus", 1)
                .eq("delflag", 0)
                .eq("username", UserContext.getUsername()));
        if (linkDOS == null || linkDOS.isEmpty()) {
            return false;
        }
        List<String> fullShortUrls = linkDOS.stream()
                .map(LinkDO::getFullshorturl)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (fullShortUrls.isEmpty()) {
            return false;
        }

        try {
            boolean updated = update(null, new UpdateWrapper<LinkDO>()
                    .eq("gid", gid)
                    .eq("delflag", 0)
                    .in("id", ids)
                    .set("enablestatus", 0));
            if (!updated) {
                log.warn("saveRecycleLink: update link enablestatus returned false, gid={}, ids={}", gid, ids);
            }
        } catch (Exception e) {
            log.error("更新 link 表 enablestatus 失败, gid={}, ids={}, error={}", gid, ids, e.getMessage(), e);
            throw e;
        }

        try {
            int rows = shortlink2GidMapper.update(null, new UpdateWrapper<Shortlink2GidDO>()
                    .in("fullshorturl", fullShortUrls)
                    .eq("gid", gid)
                    .set("delflag", 1));
            // mapper.update may return affected rows (int) depending on mapper signature; if not, ignore
            if (rows == 0) {
                log.warn("saveRecycleLink: shortlink2GidMapper.update affected 0 rows, gid={}, fullShortUrlsSize={}", gid, fullShortUrls.size());
            }
        } catch (Exception e) {
            log.error("更新 shortlink2gid 表 delflag 失败, gid={}, fullshorturls={}, error={}", gid, fullShortUrls, e.getMessage(), e);
            throw e;
        }
        redisDelayedDoubleDeleteService.deleteNowAndDelayAfterCommit(fullShortUrls);
        return true;
    }

    @Override
    public IPage<LinkRespDTO> getPageRecycleLink(Integer current, Integer size){
        String username = UserContext.getUsername();
        if (username == null || username.trim().isEmpty()) {
            // return empty page instead of null to avoid NPE in callers
            Page<LinkRespDTO> empty = new Page<>(current, size);
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        QueryWrapper<GroupDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("gid")  // 只查询 gid 字段，减少数据传输
                .eq("username", username)
                .eq("delflag", 0);
        List<String> gids = groupMapper.selectObjs(queryWrapper);
        if (gids == null || gids.isEmpty()) {
            Page<LinkRespDTO> empty = new Page<>(current,size);
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        IPage<LinkDO> linkDOIPage = page(new Page<>(current, size), new QueryWrapper<LinkDO>()
                .in("gid", gids)
                .eq("enablestatus", 0)
                .eq("delflag", 0));
        return linkDOIPage.convert(linkDO -> BeanUtil.toBean(linkDO, LinkRespDTO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean recoverLink(RecycleLinkReqDTO requestParam) {
        if (requestParam == null) {
            log.warn("recoverLink called with null requestParam");
            return false;
        }
        String gid = requestParam.getGid();
        List<String> ids = requestParam.getId();
        if (gid == null || gid.trim().isEmpty() || ids == null || ids.isEmpty()) {
            return false;
        }
        List<LinkDO> linkDOS = list(new QueryWrapper<LinkDO>()
                .eq("gid", gid)
                .in("id", ids)
                .eq("enablestatus", 0)
                .eq("delflag", 0)
                .eq(("username"), UserContext.getUsername()));
        if (linkDOS == null || linkDOS.isEmpty()) {
            return false;
        }
        UpdateWrapper<LinkDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("gid", gid)
                .in("id", ids)
                .set("enablestatus", 1)
                .set("validdatetype",1)
                .set("validdate", LocalDateTime.now().plusDays(30));  // 直接设置更新字段

        List<String> fullShortUrls = linkDOS.stream()
                .map(LinkDO::getFullshorturl)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (fullShortUrls.isEmpty()) {
            return false;
        }

        try {
            int rows = shortlink2GidMapper.update(null, new UpdateWrapper<Shortlink2GidDO>()
                    .in("fullshorturl", fullShortUrls)
                    .eq("gid", gid)
                    .set("delflag", 0));
            if (rows == 0) {
                log.warn("recoverLink: shortlink2GidMapper.update affected 0 rows, gid={}, fullShortUrlsSize={}", gid, fullShortUrls.size());
            }
        } catch (Exception e) {
            log.error("更新 shortlink2gid 表 delflag 失败, gid={}, fullshorturls={}, error={}", gid, fullShortUrls, e.getMessage(), e);
            throw e;
        }
        boolean updated = update(null, updateWrapper);
        if (!updated) {
            log.warn("recoverLink: update link enablestatus returned false, gid={}, ids={}", gid, ids);
        }
        redisDelayedDoubleDeleteService.deleteNowAndDelayAfterCommit(fullShortUrls);
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRecycleLink(RecycleLinkReqDTO requestParam) {
        if (requestParam == null) {
            log.warn("deleteRecycleLink called with null requestParam");
            return false;
        }
        String gid = requestParam.getGid();
        List<String> ids = requestParam.getId();
        if (gid == null || gid.trim().isEmpty() || ids == null || ids.isEmpty()) {
            return false;
        }

        List<LinkDO> linkDOS = list(new QueryWrapper<LinkDO>()
                .eq("gid", gid)
                .in("id", ids)
                .eq("delflag", 0)
                .eq("username", UserContext.getUsername()));
        if (linkDOS == null || linkDOS.isEmpty()) {
            return false;
        }
        List<String> fullShortUrls = linkDOS.stream()
                .map(LinkDO::getFullshorturl)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        UpdateWrapper<LinkDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("gid", gid)
                .eq("username", UserContext.getUsername())
                .in("id", ids)
                .set("delflag", 1);  // 直接设置更新字段
        boolean updated = update(null, updateWrapper);
        if (!updated) {
            log.warn("deleteRecycleLink: update delflag returned false, gid={}, ids={}", gid, ids);
        }

        shortlink2GidMapper.update(null, new UpdateWrapper<Shortlink2GidDO>()
                .in("fullshorturl", fullShortUrls)
                .eq("gid", gid)
                .set("delflag", 1));
        redisDelayedDoubleDeleteService.deleteNowAndDelayAfterCommit(fullShortUrls);
        return updated;
    }
}
