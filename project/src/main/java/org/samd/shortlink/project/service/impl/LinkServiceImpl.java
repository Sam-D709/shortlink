package org.samd.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.samd.shortlink.project.common.conversion.exception.ServiceException;
import org.samd.shortlink.project.common.util.HashUtil;
import org.samd.shortlink.project.common.util.RandomCodeUtil;
import org.samd.shortlink.project.common.util.UserContext;
import org.samd.shortlink.project.common.util.UvStatsContext;
import org.samd.shortlink.project.dao.entity.LinkDO;
import org.samd.shortlink.project.dao.entity.Shortlink2GidDO;
import org.samd.shortlink.project.dao.mapper.LinkMapper;
import org.samd.shortlink.project.dao.mapper.Shortlink2GidMapper;
import org.samd.shortlink.project.dto.req.LinkCreateReqDTO;
import org.samd.shortlink.project.dto.req.LinkUpdateBaseReqDTO;
import org.samd.shortlink.project.dto.req.LinkUpdateGidReqDTO;
import org.samd.shortlink.project.dto.resp.LinkGroupCountQueryRespDTO;
import org.samd.shortlink.project.dto.resp.LinkRespDTO;
import org.samd.shortlink.project.mq.entity.StatsMessage;
import org.samd.shortlink.project.mq.main.StatsProducer;
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
    private final StatsProducer statsProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LinkRespDTO createLink(LinkCreateReqDTO requestParam) {
        String shortlink;
        String domain = requestParam.getDomain().toLowerCase();
        shortlink = generateShortCode(requestParam.getOriginurl(), domain);
        String fullshortlink = domain + "/" + shortlink;
        LinkDO linkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        linkDO.setShorturl(shortlink);
        linkDO.setDomain(domain);
        linkDO.setFullshorturl(fullshortlink);
        linkDO.setUsername(UserContext.getUsername());
        Shortlink2GidDO gotoDO = new Shortlink2GidDO();
        gotoDO.setFullshorturl(fullshortlink);
        gotoDO.setGid(linkDO.getGid());
        if (linkDO.getValiddatetype() == 1 && requestParam.getValiddate() != null) {
            LocalDateTime validdate = requestParam.getValiddate();
            linkDO.setValiddate(validdate);
            long minutes = java.time.Duration.between(LocalDateTime.now(), validdate).toMinutes();
            if (minutes > 0 && minutes < 525600) { // 1年内
                minutes = Math.min(minutes, 43200); // 最多30天
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_FULL_SHORT_LINK_KEY, fullshortlink),
                        linkDO.getOriginurl(), minutes, MINUTES);
            } else if (minutes <= 0) {
                // 已过期，放入NULL_KEY并删除KEY
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullshortlink),
                        "-", 30, MINUTES);
                stringRedisTemplate.delete(String.format(GOTO_FULL_SHORT_LINK_KEY, fullshortlink));
            } else {
                // 超过最大分钟数，设置为43200分钟（30天）
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_FULL_SHORT_LINK_KEY, fullshortlink),
                        linkDO.getOriginurl(), 43200, MINUTES);
            }
        } else {
            linkDO.setValiddatetype(0);
            linkDO.setValiddate(null);
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_FULL_SHORT_LINK_KEY, fullshortlink),
                    linkDO.getOriginurl(), 43200, MINUTES);
        }
        try {
            save(linkDO);
        } catch (DuplicateKeyException e) {
            log.warn("短链接已存在，原始链接：{}，短链接：{}", requestParam.getOriginurl(), fullshortlink);
            throw new ServiceException("短链接已存在，请勿重复创建");
        }
        try {
            shortlink2GidMapper.insert(gotoDO);
        } catch (DuplicateKeyException e) {
            log.warn("短链接与分组映射已存在，短链接：{}，分组：{}", fullshortlink, linkDO.getGid());
            throw new ServiceException("短链接和gid映射表出错,请联系管理员或者重新创建短链接");
        }
        linkBloomFilter.add(fullshortlink);
        stringRedisTemplate.delete(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullshortlink));
        return BeanUtil.toBean(linkDO, LinkRespDTO.class);
    }

    @Override
    public IPage<LinkRespDTO> getPageLink(String gid, long current, long size) {
        QueryWrapper<LinkDO> qw = new QueryWrapper<>();
        qw.eq("gid", gid)
                .eq("enablestatus", 1)
                .eq("delflag", 0)
                .eq(("username"), UserContext.getUsername())
                .orderByDesc("createtime");
        IPage<LinkDO> linkDOIPage = this.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size), qw);
        return linkDOIPage.convert(linkDO -> BeanUtil.toBean(linkDO, LinkRespDTO.class));
    }

    @Override
    public List<LinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<LinkDO> qw = new QueryWrapper<>();
        qw.select("gid","count(*) as linkCount")
                .eq("enablestatus", 1)
                .eq("delflag",0)
                .eq(("username"), UserContext.getUsername())
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
                .eq("delflag", 0)
                .eq(("username"), UserContext.getUsername()));
        if (oldLinkDO == null) {
            throw new ServiceException("短链接不存在");
        }
        String domain = oldLinkDO.getDomain();
        String fullShortUrl = oldLinkDO.getFullshorturl();

        LinkDO updatedLinkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        updatedLinkDO.setDomain(domain);
        updatedLinkDO.setFullshorturl(fullShortUrl);
        if (requestParam.getValiddatetype() == 1 && requestParam.getValiddate() != null) {
            LocalDateTime validdate = requestParam.getValiddate();
            updatedLinkDO.setValiddate(validdate);
            long minutes = java.time.Duration.between(LocalDateTime.now(), validdate).toMinutes();
            if (minutes > 0 && minutes < 525600) {
                minutes = Math.min(minutes, 43200);
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl),
                        requestParam.getOriginurl(), minutes, MINUTES);
            } else if (minutes <= 0) {
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl),
                        "-", 30, MINUTES);
                stringRedisTemplate.delete(String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl));
            } else {
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl),
                        requestParam.getOriginurl(), 43200, MINUTES);
            }
        } else {
            requestParam.setValiddatetype(0);
            requestParam.setValiddate(null);
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl),
                    requestParam.getOriginurl(), 43200, MINUTES);
        }
        stringRedisTemplate.delete(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl));
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
                .eq("delflag", 0)
                .eq(("username"), UserContext.getUsername()));
        if (oldLinkDO == null) {
            throw new ServiceException("短链接不存在");
        }
        String lockKey = String.format("rwlock:shortlink:%s", oldLinkDO.getFullshorturl());
        RLock writeLock = redissonClient.getReadWriteLock(lockKey).writeLock();
        writeLock.lock();
        try {
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

            shortlink2GidMapper.update(null, new UpdateWrapper<Shortlink2GidDO>()
                    .set("delflag", 0)
                    .eq("fullshorturl", oldLinkDO.getFullshorturl())
                    .eq("gid", requestParam.getNewGid()));
        } finally {
            writeLock.unlock();
        }
        return true;
    }

    @Override
    public ResponseEntity<Void> link2Orginurl(String shortlink) {
        String fullShortUrl = UvStatsContext.getDomain() + "/" + shortlink;
        String originLink;
        RLock readLock = redissonClient.getReadWriteLock("rwlock:shortlink:" + fullShortUrl).readLock();
        readLock.lock();
        try {
            // 从 Redis 中获取原始链接
            originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl));
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
                        String nullLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl));
                        if (StrUtil.isNotBlank(nullLink)) {
                            throw new ServiceException("短链接不存在或已经删除");
                        }
                        // 查询数据库
                        Shortlink2GidDO gotoDo = shortlink2GidMapper.selectOne(new QueryWrapper<Shortlink2GidDO>()
                                .eq("fullshorturl", fullShortUrl)
                                .eq("delflag", 0));
                        if (gotoDo == null) {
                            // 设置标记为不存在的键，避免重复查询
                            stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl), "-", 1, DAYS);
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
                                throw new ServiceException("短链接不存在或已经删除");
                            } else if (linkDO.getValiddatetype() == 0) {
                                stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl), originLink, 30, DAYS);
                            } else if (linkDO.getValiddatetype() == 1 && linkDO.getValiddate().isAfter(LocalDateTime.now())) {
                                long MIN = Math.min(LocalDateTime.now().until(linkDO.getValiddate(), ChronoUnit.MINUTES), 1800);
                                stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_KEY, fullShortUrl), originLink, MIN, MINUTES);
                            }else{
                                stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl), "-", 1, DAYS);
                                throw new ServiceException("短链接不存在或已经删除");
                            }
                        }else {
                            stringRedisTemplate.opsForValue().set(String.format(GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl), "-", 1, DAYS);
                            throw new ServiceException("短链接不存在或已经删除");
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        }finally {
            readLock.unlock();
        }
        LocalDateTime now = LocalDateTime.now();
        StatsMessage message = StatsMessage.builder()
                .fullShortUrl(fullShortUrl)
                .date(new Date())
                .hour(now.getHour())
                .year(now.getYear())
                .month(now.getMonthValue())
                .uvFirstFlag(UvStatsContext.isUvFirst())
                .uvDayFirstFlag(UvStatsContext.isUvDayFirst())
                .uvMonthFirstFlag(UvStatsContext.isUvMonthFirst())
                .os(UvStatsContext.getOs())
                .browser(UvStatsContext.getBrowser())
                .device(UvStatsContext.getDevice())
                .build();
        statsProducer.send(message);
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

}
