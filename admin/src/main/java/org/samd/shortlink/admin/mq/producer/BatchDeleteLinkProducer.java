package org.samd.shortlink.admin.mq.producer;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.samd.shortlink.admin.mq.entity.BatchDeleteLinkMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

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
        // 将每个gid包装为BatchDeleteLinkMessage，批量发送
        for (String gid : gidList) {
            BatchDeleteLinkMessage message = new BatchDeleteLinkMessage(gid);
            Message<BatchDeleteLinkMessage> mqMsg = MessageBuilder.withPayload(message).build();
            rocketMQTemplate.syncSend(topic, mqMsg);
            log.info("[BatchDeleteLinkProducer] 已发送批量删除短链消息，gid={}", gid);
        }
    }
}
