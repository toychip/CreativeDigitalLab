package com.chat.domain.event;

import java.time.Instant;

/**
 * 사용자 입장/퇴장 이벤트
 */
public record UserEvent(
    String eventId,
    String sessionId,
    long seq,
    Instant createdAt,
    String userId,
    Type type
) implements ChatEvent {

    public enum Type {
        JOINED,
        LEFT
    }
}
