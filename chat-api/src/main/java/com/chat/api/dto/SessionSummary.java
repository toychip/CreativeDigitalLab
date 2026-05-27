package com.chat.api.dto;

import com.chat.application.session.SessionEntity;
import com.chat.domain.session.SessionStatus;

import java.time.LocalDateTime;

public record SessionSummary(
        String sessionId,
        SessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static SessionSummary from(SessionEntity entity) {
        return new SessionSummary(
                entity.getSessionId(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getEndedAt()
        );
    }
}
