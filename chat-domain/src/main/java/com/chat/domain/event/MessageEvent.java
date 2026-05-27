package com.chat.domain.event;

import com.chat.domain.common.IdGenerator;
import com.chat.domain.session.MessageStatus;

import java.time.Instant;

/**
 * 메시지 전송/수정/삭제 이벤트
 */
public record MessageEvent(
    String eventId,
    String sessionId,
    String clientEventId,
    long seq,
    Instant createdAt,
    String senderId,
    String messageId,
    String content,
    MessageStatus type
) implements ChatEvent {

    /**
     * 신규 이벤트 생성. eventId 와 createdAt 은 도메인이 발급.
     * messageId 는 호출부에서 결정 (SENT 면 새 ID, EDITED/DELETED 면 기존 메시지 ID 재사용).
     */
    public static MessageEvent create(
        String sessionId,
        String clientEventId,
        long seq,
        String senderId,
        String messageId,
        String content,
        MessageStatus type
    ) {
        return new MessageEvent(
            IdGenerator.generate(),
            sessionId,
            clientEventId,
            seq,
            Instant.now(),
            senderId,
            messageId,
            content,
            type
        );
    }
}
