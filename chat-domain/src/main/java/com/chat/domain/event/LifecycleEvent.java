package com.chat.domain.event;

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
    long seq,
    Instant createdAt,
    SessionStatus status
) implements ChatEvent {
}
