package org.samd.shortlink.project.mq.main;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.samd.shortlink.project.common.constant.RedisCacheConstant;
import org.samd.shortlink.project.dao.entity.LinkDO;
import org.samd.shortlink.project.dao.mapper.LinkMapper;
import org.samd.shortlink.project.mq.entity.BatchDeleteLinkMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
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
public class BatchDeleteLinkConsumer implements RocketMQListener<List<BatchDeleteLinkMessage>> {

    private final LinkMapper linkMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Override
    public void onMessage(List<BatchDeleteLinkMessage> messageList) {
        // 批量处理每个gid
        for (BatchDeleteLinkMessage msg : messageList) {
            handleDelete(msg.getGid());
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
                // 逻辑删除
                linkMapper.update(null, new UpdateWrapper<LinkDO>()
                        .eq("id", link.getId())
                        .set("delflag", 1));
                // 删除Redis缓存
                stringRedisTemplate.delete(String.format(RedisCacheConstant.GOTO_FULL_SHORT_LINK_KEY, fullShortUrl));
                log.info("[BatchDeleteLinkConsumer] 已逻辑删除短链并清理缓存，fullShortUrl={}", fullShortUrl);
            } finally {
                writeLock.unlock();
            }
        }
    }
}
