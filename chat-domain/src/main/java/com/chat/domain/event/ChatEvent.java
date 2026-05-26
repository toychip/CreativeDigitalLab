package com.chat.domain.event;

import java.time.Instant;

/**
 * 채팅에서 발생하는 모든 이벤트의 추상 부모
 */
public sealed interface ChatEvent
    permits LifecycleEvent, UserEvent, MessageEvent {

    String eventId();

    String sessionId();

    String clientEventId();

    long seq();

    Instant createdAt();
}
