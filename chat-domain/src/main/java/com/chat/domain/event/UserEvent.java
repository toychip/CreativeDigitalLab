package com.chat.domain.event;

import com.chat.domain.common.IdGenerator;

import java.time.Instant;

/**
 * 사용자 입장/퇴장 이벤트
 */
public record UserEvent(
    String eventId,
    String sessionId,
    String clientEventId,
    long seq,
    Instant createdAt,
    String userId,
    Type type
) implements ChatEvent {

    public enum Type {
        JOINED,
        LEFT
    }

    /**
     * 신규 이벤트 생성. eventId 와 createdAt 은 도메인이 발급.
     */
    public static UserEvent create(
        String sessionId,
        String clientEventId,
        long seq,
        String userId,
        Type type
    ) {
        return new UserEvent(
            IdGenerator.generate(),
            sessionId,
            clientEventId,
            seq,
            Instant.now(),
            userId,
            type
        );
    }
}
