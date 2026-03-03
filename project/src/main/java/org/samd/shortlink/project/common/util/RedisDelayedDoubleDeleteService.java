package org.samd.shortlink.project.common.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.samd.shortlink.project.common.constant.RedisCacheConstant;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存延时双删：先删一次，事务提交后延时再删一次。
 */
@Slf4j
@Component
public class RedisDelayedDoubleDeleteService implements DisposableBean {

    private final StringRedisTemplate stringRedisTemplate;
    private final ScheduledExecutorService scheduler;

    @Value("${shortlink.cache.double-delete.delay-ms:" + RedisCacheConstant.DELAY_DELETE_SHORT_LINK_CACHE_MILLIS + "}")
    private long delayMillis;

    public RedisDelayedDoubleDeleteService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("shortlink-cache-double-delete");
            thread.setDaemon(true);
            return thread;
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    public void deleteNowAndDelayAfterCommit(String fullShortUrl) {
        if (StrUtil.isBlank(fullShortUrl)) {
            return;
        }
        List<String> keys = buildShortLinkCacheKeys(fullShortUrl.trim());
        deleteNow(keys);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    scheduleDelete(keys);
                }
            });
            return;
        }
        scheduleDelete(keys);
    }

    public void deleteNowAndDelay(String fullShortUrl) {
        if (StrUtil.isBlank(fullShortUrl)) {
            return;
        }
        List<String> keys = buildShortLinkCacheKeys(fullShortUrl.trim());
        deleteNow(keys);
        scheduleDelete(keys);
    }

    public void deleteNowAndDelayAfterCommit(List<String> fullShortUrls) {
        if (fullShortUrls == null || fullShortUrls.isEmpty()) {
            return;
        }
        fullShortUrls.forEach(this::deleteNowAndDelayAfterCommit);
    }

    private List<String> buildShortLinkCacheKeys(String fullShortUrl) {
        return Arrays.asList(
                String.format(RedisCacheConstant.GOTO_FULL_SHORT_LINK_KEY, fullShortUrl),
                String.format(RedisCacheConstant.GOTO_FULL_SHORT_LINK_NULL_KEY, fullShortUrl)
        );
    }

    private void deleteNow(List<String> keys) {
        try {
            stringRedisTemplate.delete(keys);
        } catch (Exception ex) {
            log.warn("删除短链缓存失败 keys={}, error={}", keys, ex.getMessage());
        }
    }

    private void scheduleDelete(List<String> keys) {
        List<String> keySnapshot = new ArrayList<>(keys);
        scheduler.schedule(() -> {
            try {
                stringRedisTemplate.delete(keySnapshot);
            } catch (Exception ex) {
                log.warn("延时删除短链缓存失败 keys={}, error={}", keySnapshot, ex.getMessage());
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }
}

