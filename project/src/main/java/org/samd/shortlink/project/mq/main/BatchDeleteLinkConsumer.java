package org.samd.shortlink.project.mq.main;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.samd.shortlink.project.common.util.RedisDelayedDoubleDeleteService;
import org.samd.shortlink.project.dao.entity.LinkDO;
import org.samd.shortlink.project.dao.entity.Shortlink2GidDO;
import org.samd.shortlink.project.dao.mapper.LinkMapper;
import org.samd.shortlink.project.dao.mapper.Shortlink2GidMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 批量删除短链消息消费者
 * 监听BATCH_DELETE_LINK主题，收到gid后批量逻辑删除对应短链并清理缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${mq.batchDeleteLink.topic:BATCH_DELETE_LINK}",
        consumerGroup = "${mq.batchDeleteLink.consumerGroup:batch-delete-link-group}"
)
public class BatchDeleteLinkConsumer implements RocketMQListener<List<String>> {

    private final LinkMapper linkMapper;
    private final Shortlink2GidMapper shortlink2GidMapper;
    private final RedissonClient redissonClient;
    private final RedisDelayedDoubleDeleteService redisDelayedDoubleDeleteService;

    @Override
    public void onMessage(List<String> messageList) {
        // 批量处理每个gid
        for (String msg : messageList) {
            handleDelete(msg);
        }
    }

    /**
     * 根据gid批量逻辑删除短链并清理缓存
     */
    private void handleDelete(String gid) {
        log.info("[BatchDeleteLinkConsumer] 收到批量删除短链消息，gid={}", gid);
        // 查询所有该gid下的短链
        List<LinkDO> linkList = linkMapper.selectList(new QueryWrapper<LinkDO>()
                .eq("gid", gid)
                .eq("delflag", 0));
        for (LinkDO link : linkList) {
            String fullShortUrl = link.getFullshorturl();
            String lockKey = String.format("rwlock:shortlink:%s", fullShortUrl);
            RLock writeLock = redissonClient.getReadWriteLock(lockKey).writeLock();
            writeLock.lock();
            try {
                // 逻辑删除 link 表
                linkMapper.update(null, new UpdateWrapper<LinkDO>()
                        .eq("gid", gid)
                        .eq("id", link.getId())
                        .set("delflag", 1));

                // 逻辑删除 shortlink2gid 表（按分片键 fullshorturl 路由）
                shortlink2GidMapper.update(null, new UpdateWrapper<Shortlink2GidDO>()
                        .eq("fullshorturl", fullShortUrl)
                        .eq("gid", gid)
                        .eq("delflag", 0)
                        .set("delflag", 1));

                // 延时双删缓存
                redisDelayedDoubleDeleteService.deleteNowAndDelay(fullShortUrl);
                log.info("[BatchDeleteLinkConsumer] 已逻辑删除短链及映射并清理缓存，fullShortUrl={}", fullShortUrl);
            } finally {
                writeLock.unlock();
            }
        }
    }
}
