package org.samd.shortlink.project.mq.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 延迟重试消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelayRetryMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    // 原始消息ID
    private String originalMessageId;

    // 延迟队列专用ID
    private String retryId;

    // 重试次数
    private Integer retryCount;

    // 最大重试次数
    private Integer maxRetryCount;

    // 延迟秒数（指数退避）
    private Long delaySeconds;

    // 原始消息内容（JSON序列化后）
    private String payloadJson;

    // 消息类型
    private String messageType;

    // 首次创建时间
    private LocalDateTime firstCreateTime;

    // 当前重试时间
    private LocalDateTime currentRetryTime;

    // 失败原因
    private String lastFailReason;
}