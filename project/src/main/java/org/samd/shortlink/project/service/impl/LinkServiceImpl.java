package org.samd.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.samd.shortlink.project.common.conversion.exception.ServiceException;
import org.samd.shortlink.project.common.util.HashUtil;
import org.samd.shortlink.project.common.util.LinkMonitorUtil;
import org.samd.shortlink.project.common.util.RandomCodeUtil;
import org.samd.shortlink.project.dao.entity.*;
import org.samd.shortlink.project.dao.mapper.*;
import org.samd.shortlink.project.dto.req.LinkCreateReqDTO;
import org.samd.shortlink.project.dto.req.LinkPageReqDTO;
import org.samd.shortlink.project.dto.req.LinkUpdateBaseReqDTO;
import org.samd.shortlink.project.dto.req.LinkUpdateGidReqDTO;
import org.samd.shortlink.project.dto.resp.LinkGroupCountQueryRespDTO;
import org.samd.shortlink.project.dto.resp.LinkRespDTO;
import org.samd.shortlink.project.service.LinkService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.samd.shortlink.project.common.constant.RedisCacheConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements LinkService {

    private final RBloomFilter<String> linkBloomFilter;
    private final Shortlink2GidMapper shortlink2GidMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final AccessStateHourMapper accessStateHourMapper;
    private final OSStateMapper osStateMapper;
    private final BrowserStateMapper browserStateMapper;
    private final DeviceStateMapper deviceStateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LinkRespDTO createLink(LinkCreateReqDTO requestParam) {
        String shortlink;
        String domain = requestParam.getDomain().toLowerCase(); // Convert domain to lowercase
        shortlink = generateShortCode(requestParam.getOriginurl(), domain);
        String fullshortlink = domain + "/" + shortlink;
        LinkDO linkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        linkDO.setShorturl(shortlink);
        linkDO.setDomain(domain);
        linkDO.setFullshorturl(fullshortlink);
        Shortlink2GidDO gotoDO = new Shortlink2GidDO();
        gotoDO.setFullshorturl(fullshortlink);
        gotoDO.setGid(linkDO.getGid());
        if(linkDO.getValiddatetype() == 1 && requestParam.getValiddate() > 0 && requestParam.getValiddate() < 366){
            int days = Math.min(requestParam.getValiddate(),30);
            LocalDateTime validdate = LocalDateTime.now().plusDays(requestParam.getValiddate());
            linkDO.setValiddate(validdate);
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_FULL_SHORT_LINK_KEY,fullshortlink),
                    linkDO.getOriginurl(),days, DAYS);
        }else{
            linkDO.setValiddatetype(0);
            linkDO.setValiddate(null);
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_FULL_SHORT_LINK_KEY,fullshortlink),
                    linkDO.getOriginurl(),30, DAYS);
        }
        try{
            save(linkDO);
        }catch (DuplicateKeyException e){
            log.warn("短链接已存在，原始链接：{}，短链接：{}", requestParam.getOriginurl(), fullshortlink);
            throw new ServiceException("短链接已存在，请勿重复创建");
        }
        try{
            shortlink2GidMapper.insert(gotoDO);
        }catch (DuplicateKeyException e){
            log.warn("短链接与分组映射已存在，短链接：{}，分组：{}", fullshortlink, linkDO.getGid());
            throw new ServiceException("短链接和gid映射表出错,请联系管理员或者重新创建短链接");
        }
        linkBloomFilter.add(fullshortlink);
        return BeanUtil.toBean(linkDO, LinkRespDTO.class);
    }

    @Override
    public IPage<LinkRespDTO> getPageLink(LinkPageReqDTO requestParam) {
        QueryWrapper<LinkDO> qw = new QueryWrapper<>();
        qw.eq("gid", requestParam.getGid())
                .eq("enablestatus", 1)
                .eq("delflag", 0)
                .orderByDesc("createtime");
        IPage<LinkDO> linkDOIPage = page(requestParam, qw);
        return linkDOIPage.convert(linkDO -> BeanUtil.toBean(linkDO, LinkRespDTO.class));
    }

    @Override
    public List<LinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<LinkDO> qw = new QueryWrapper<>();
        qw.select("gid","count(*) as linkCount")
                .eq("enablestatus", 1)
                .eq("delflag",0)
                .in("gid",requestParam)
                .groupBy("gid");
        List<Map<String, Object>> result = listMaps(qw);
        return BeanUtil.copyToList(result, LinkGroupCountQueryRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateLinkBase(LinkUpdateBaseReqDTO requestParam) {
        LinkDO oldLinkDO = getOne(new QueryWrapper<LinkDO>()
                .eq("gid", requestParam.getGid())
                .eq("id", requestParam.getId())
                .eq("enablestatus", 1)
                .eq("delflag", 0));
        if (oldLinkDO == null) {
            throw new ServiceException("短链接不存在");
        }

        String domain = requestParam.getDomain().toLowerCase();
        String newFullShortUrl = domain.equals(oldLinkDO.getDomain())
                ? oldLinkDO.getFullshorturl()
                : domain + "/" + oldLinkDO.getShorturl();

        if (!domain.equals(oldLinkDO.getDomain())) {
            shortlink2GidMapper.update(null, new UpdateWrapper<Shortlink2GidDO>()
                    .eq("fullshorturl", oldLinkDO.getFullshorturl())
                    .eq("gid", requestParam.getGid())
                    .set("delflag", 1));

            Shortlink2GidDO existingMapping = shortlink2GidMapper.selectOne(new QueryWrapper<Shortlink2GidDO>()
                    .eq("fullshorturl", newFullShortUrl)
                    .eq("gid", requestParam.getGid()));

            if (existingMapping != null) {
                if (existingMapping.getDelflag() == 1) {
                    shortlink2GidMapper.update(null, new UpdateWrapper<Shortlink2GidDO>()
                            .eq("fullshorturl", newFullShortUrl)
                            .eq("gid", requestParam.getGid())
                            .set("delflag", 0));
                } else {
                    log.info("短链接与分组映射已存在，短链接：{}，分组：{}", newFullShortUrl, requestParam.getGid());
                }
            linkBloomFilter.add(newFullShortUrl);
            stringRedisTemplate.delete(String.format(GOTO_FULL_SHORT_LINK_KEY, newFullShortUrl));

            } else {
                Shortlink2GidDO newMapping = new Shortlink2GidDO();
                newMapping.setFullshorturl(newFullShortUrl);
                newMapping.setGid(requestParam.getGid());
                shortlink2GidMapper.insert(newMapping);
            }
        }
        LinkDO updatedLinkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        updatedLinkDO.setDomain(domain);
        updatedLinkDO.setFullshorturl(newFullShortUrl);
        if(requestParam.getValiddatetype() == 1 && requestParam.getValiddate() > 0 && requestParam.getValiddate() < 366){
            int days = Math.min(requestParam.getValiddate(),30);
            LocalDateTime validdate = LocalDateTime.now().plusDays(requestParam.getValiddate());
            updatedLinkDO.setValiddate(validdate);
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_FULL_SHORT_LINK_KEY,newFullShortUrl),
                    requestParam.getOriginurl(),days, DAYS);
        }else{
            requestParam.setValiddatetype(0);
            requestParam.setValiddate(null);
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_FULL_SHORT_LINK_KEY,newFullShortUrl),
                    requestParam.getOriginurl(),30, DAYS);
        }
        update(updatedLinkDO, new UpdateWrapper<LinkDO>()
                .eq("gid", requestParam.getGid())
                .eq("id", requestParam.getId()));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateLinkGid(LinkUpdateGidReqDTO requestParam) {
        LinkDO oldLinkDO = getOne(new QueryWrapper<LinkDO>()
                .eq("gid", requestParam.getOldGid())
                .eq("id", requestParam.getId())
                .eq("enablestatus", 1)
                .eq("delflag", 0));
        if (oldLinkDO == null) {
            throw new ServiceException("短链接不存在");
        }

        LinkDO existingLinkDO = getOne(new QueryWrapper<LinkDO>()
                .eq("gid", requestParam.getNewGid())
                .eq("fullshorturl", oldLinkDO.getFullshorturl()));

        if (existingLinkDO != null) {
            update(null, new UpdateWrapper<LinkDO>()
                    .eq("gid", requestParam.getNewGid())
                    .eq("id", existingLinkDO.getId())
                    .set("delflag", 0)
                    .set("enablestatus", 1));
        } else {
            oldLinkDO.setGid(requestParam.getNewGid());
            oldLinkDO.setId(null);
            oldLinkDO.setDelflag(0);
            oldLinkDO.setEnablestatus(1);
            save(oldLinkDO);
        }

        update(null, new UpdateWrapper<LinkDO>()
                .eq("gid", requestParam.getOldGid())
                .eq("id", requestParam.getId())
                .eq("delflag", 0)
                .set("delflag", 1));

        Shortlink2GidDO existingMapping = shortlink2GidMapper.selectOne(new QueryWrapper<Shortlink2GidDO>()
                .eq("fullshorturl", oldLinkDO.getFullshorturl())
                .eq("gid", requestParam.getNewGid()));

        if (existingMapping != null) {
            shortlink2GidMapper.update(null, new UpdateWrapper<Shortlink2GidDO>()
                    .set("delflag", 0)
                    .eq("fullshorturl", oldLinkDO.getFullshorturl())
                    .eq("gid", requestParam.getNewGid()));
        } else {
            Shortlink2GidDO newMapping = new Shortlink2GidDO();
            newMapping.setFullshorturl(oldLinkDO.getFullshorturl());
            newMapping.setGid(requestParam.getNewGid());
            shortlink2GidMapper.insert(newMapping);
        }

        shortlink2GidMapper.update(null, new UpdateWrapper<Shortlink2GidDO>()
                .set("delflag", 1)
                .eq("fullshorturl", oldLinkDO.getFullshorturl())
                .eq("gid", requestParam.getOldGid()));
        return true;
    }

    @Override
    public ResponseEntity<Void> link2Orginurl(String shortlink, HttpServletRequest request, HttpServletResponse response) {
        // 获取协议
        String protocol = request.getHeader("X-Forwarded-Proto");
        if (StrUtil.isBlank(protocol)) {
            protocol = request.getHeader("X-Scheme");
        }
        if (StrUtil.isBlank(protocol)) {
            protocol = request.getScheme();
        }
        // 获取域名并去除端口号
        String host = request.getHeader("Host");
        String domain = host != null ? host.split(":")[0] : "";
        log.info("访问域名为  {}", domain);
        log.info("访问协议为  {}", protocol);
        String fullShortUrl = domain + "/" + shortlink;

        // 从 Redis 中获取原始链接
        String originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl));
        if (originLink == null) {
            // 检查布隆过滤器和 Redis 标记
            if (!linkBloomFilter.contains(fullShortUrl) ||
                    StrUtil.isNotBlank(stringRedisTemplate.opsForValue().get(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl)))) {
                throw new ServiceException("短链接不存在或已经删除");
            }
            // 获取分布式锁
            RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
            lock.lock();
            try {
                // 再次检查 Redis
                originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl));
                if (originLink == null) {
                    // 查询数据库
                    Shortlink2GidDO gotoDo = shortlink2GidMapper.selectOne(new QueryWrapper<Shortlink2GidDO>()
                            .eq("fullshorturl", fullShortUrl)
                            .eq("delflag", 0));
                    if (gotoDo == null) {
                        // 设置标记为不存在的键，避免重复查询
                        stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl), "-", 1, DAYS);
                        lock.unlock();
                        throw new ServiceException("短链接不存在或已经删除");
                    }

                    LinkDO linkDO = getOne(new QueryWrapper<LinkDO>()
                            .eq("gid", gotoDo.getGid())
                            .eq("fullshorturl", fullShortUrl)
                            .eq("enablestatus", 1)
                            .eq("delflag", 0));
                        // 如果短链接已过期或被标记为删除，更新 delflag 并设置 Redis 标记
                    if (linkDO != null ) {
                        originLink = linkDO.getOriginurl();
                        if (linkDO.getValiddatetype() == 1 && linkDO.getValiddate().isBefore(LocalDateTime.now())) {
                            update(null, new UpdateWrapper<LinkDO>()
                                    .eq("gid", gotoDo.getGid())
                                    .eq("id", linkDO.getId())
                                    .set("enablestatus", 0));
                            shortlink2GidMapper.update(new UpdateWrapper<Shortlink2GidDO>()
                                    .eq("fullshorturl", fullShortUrl)
                                    .eq("gid", gotoDo.getGid())
                                    .set("delflag", 1));
                            stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl), "-", 1, DAYS);
                            lock.unlock();
                            throw new ServiceException("短链接不存在或已经删除");
                        } else if (linkDO.getValiddatetype() == 0) {
                            stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl), originLink, 30, DAYS);
                        } else if (linkDO.getValiddatetype() == 1 && linkDO.getValiddate().isAfter(LocalDateTime.now())) {
                            long MIN = Math.min(LocalDateTime.now().until(linkDO.getValiddate(), ChronoUnit.MINUTES), 1800);
                            stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl), originLink, MIN, MINUTES);
                        }else{
                            stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl), "-", 1, DAYS);
                            lock.unlock();
                            throw new ServiceException("短链接不存在或已经删除");
                        }
                    }else {
                        stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl), "-", 1, DAYS);
                        lock.unlock();
                        throw new ServiceException("短链接不存在或已经删除");
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        accessState(fullShortUrl, request, shortlink, response);
        osState(fullShortUrl, request);
        browserState(fullShortUrl, request);
        deviceState(fullShortUrl, request);
        // 返回重定向响应
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originLink))
                .build();
    }

    private String generateShortCode(String originalUrl, String domain) throws ServiceException {
        String link;
        int attempt = 0;
        do {
            String input = attempt == 0 ? originalUrl : originalUrl + RandomCodeUtil.generate();
            link = HashUtil.hashToBase62(input);
            attempt++;
        } while (linkBloomFilter.contains(domain + "/" + link) && attempt < 3);

        if (attempt == 3) {
            throw new ServiceException("短链接生成失败，请稍后重试");
        } else {
            return link;
        }
    }

    private void accessState(String fullshorturl, HttpServletRequest request, String shortlink, HttpServletResponse response) {
        boolean newVisitor = true;
        String uvValue;
        Cookie[] requestCookies = request.getCookies();

        if (ArrayUtils.isNotEmpty(requestCookies)) {
            for (Cookie cookie : requestCookies) {
                if (("sl_state_" + shortlink).equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (StrUtil.isNotBlank(value)) {
                        newVisitor = false;
                    }
                    break;
                }
            }
        }

        if (newVisitor) {
            uvValue = UUID.fastUUID().toString();
            Cookie statsCookie = new Cookie("sl_state_" + shortlink, uvValue);
            statsCookie.setPath("/" + shortlink);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextHour = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            long seconds = ChronoUnit.SECONDS.between(now, nextHour);
            statsCookie.setMaxAge((int) seconds);
            response.addCookie(statsCookie);
        }

        int hour = LocalDateTime.now().getHour();
        AccessStateHourDO accessState = new AccessStateHourDO();
        accessState.setHour(hour);
        accessState.setFullshorturl(fullshorturl);
        accessState.setPv(1);
        accessState.setUv(newVisitor ? 1 : 0);
        accessState.setDate(new Date());
        accessStateHourMapper.shortLinkState(accessState);
    }

    private void osState(String fullshorturl, HttpServletRequest request){
        OSStateDO osStateDO = new OSStateDO();
        osStateDO.setFullshorturl(fullshorturl);
        osStateDO.setDate(new Date());
        osStateDO.setOs(LinkMonitorUtil.getOSFromRequest(request));
        osStateDO.setCnt(1);
        osStateMapper.shortLinkOSState(osStateDO);
    }

    private void browserState(String fullshorturl, HttpServletRequest request){
        BrowserStateDO browserStateDO = new BrowserStateDO();
        browserStateDO.setFullshorturl(fullshorturl);
        browserStateDO.setDate(new Date());
        browserStateDO.setBrowser(LinkMonitorUtil.getBrowserFromRequest(request));
        browserStateDO.setCnt(1);
        browserStateMapper.shortLinkBrowserState(browserStateDO);
    }

    private void deviceState(String fullshorturl, HttpServletRequest request){
        DeviceStateDO deviceStateDO = new DeviceStateDO();
        deviceStateDO.setFullshorturl(fullshorturl);
        deviceStateDO.setDate(new Date());
        deviceStateDO.setDevice(LinkMonitorUtil.getDeviceFromRequest(request));
        deviceStateDO.setCnt(1);
        deviceStateMapper.shortLinkDeviceState(deviceStateDO);
    }
}
