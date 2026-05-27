package com.chat.websocket.dto;

import com.chat.domain.event.MessageEvent;
import com.chat.domain.session.MessageStatus;

import java.time.Instant;

public record ChatMessageResponse(
    MessageStatus messageStatus,
    String sessionId,
    long seq,
    Instant createdAt,
    String clientEventId,
    String senderId,
    String messageId,
    String content
) {
    public static ChatMessageResponse fromMessage(MessageEvent event) {
        return new ChatMessageResponse(
            event.type(),
            event.sessionId(),
            event.seq(),
            event.createdAt(),
            event.clientEventId(),
            event.senderId(),
            event.messageId(),
            event.content()
        );
    }
}
