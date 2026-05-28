package com.chat.application.session;

import com.chat.domain.session.SessionStatus;

import java.time.LocalDateTime;

public record SessionView(
        String sessionId,
        SessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static SessionView from(SessionEntity entity) {
        return new SessionView(
                entity.getSessionId(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getEndedAt()
        );
    }
}
