package com.chat.application.message;

import com.chat.domain.session.MessageStatus;

import java.time.LocalDateTime;

public record MessageView(
        String messageId,
        String senderId,
        String content,
        MessageStatus status,
        long seq,
        LocalDateTime createdAt
) {
    public static MessageView from(MessageEntity entity) {
        return new MessageView(
                entity.getMessageId(),
                entity.getSenderId(),
                entity.getContent(),
                entity.getStatus(),
                entity.getSeq(),
                entity.getCreatedAt()
        );
    }
}
