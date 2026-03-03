package org.samd.shortlink.admin.mq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 批量删除短链消息生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchDeleteLinkProducer {

    private final RocketMQTemplate rocketMQTemplate;

    @Value("${mq.batchDeleteLink.topic:BATCH_DELETE_LINK}")
    private String topic;

    /**
     * 批量发送批量删除短链消息
     * @param gidList 分组ID列表
     */
    public void send(List<String> gidList) {
        if (gidList == null || gidList.isEmpty()) {
            log.warn("[BatchDeleteLinkProducer] gid列表为空，不发送消息");
            return;
        }
        rocketMQTemplate.syncSend(topic, gidList);
    }
}
