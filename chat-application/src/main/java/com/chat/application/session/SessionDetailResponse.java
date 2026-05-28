package com.chat.application.session;

import com.chat.application.sessionuser.SessionUserEntity;
import com.chat.domain.session.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SessionDetailResponse(
        String sessionId,
        SessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<ParticipantView> participants
) {
    public static SessionDetailResponse of(SessionEntity session, List<SessionUserEntity> members) {
        return new SessionDetailResponse(
                session.getSessionId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt(),
                members.stream().map(ParticipantView::from).toList()
        );
    }
}
