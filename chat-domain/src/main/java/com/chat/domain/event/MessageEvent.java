package com.chat.domain.event;

import com.chat.domain.session.MessageStatus;

import java.time.Instant;

/**
 * 메시지 전송/수정/삭제 이벤트
 */
public record MessageEvent(
    String eventId,
    String sessionId,
    long seq,
    Instant createdAt,
    String senderId,
    String messageId,
    String content,
    MessageStatus type
) implements ChatEvent {
}
