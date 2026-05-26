package com.chat.domain.event;

import com.chat.domain.common.IdGenerator;
import com.chat.domain.session.SessionStatus;

import java.time.Instant;

/**
 * 세션 상태 전환 이벤트
 * status 가 ACTIVE → 시작 또는 재개
 * status 가 SUSPENDED → 중단
 * status 가 ENDED → 종료
 */
public record LifecycleEvent(
    String eventId,
    String sessionId,
    String clientEventId,
    long seq,
    Instant createdAt,
    SessionStatus status
) implements ChatEvent {

    public static LifecycleEvent create(
        String sessionId,
        String clientEventId,
        long seq,
        SessionStatus status
    ) {
        return new LifecycleEvent(
            IdGenerator.generate(),
            sessionId,
            clientEventId,
            seq,
            Instant.now(),
            status
        );
    }
}
