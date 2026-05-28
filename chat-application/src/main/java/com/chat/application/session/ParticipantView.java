package com.chat.application.session;

import com.chat.application.sessionuser.MemberRole;
import com.chat.application.sessionuser.SessionUserEntity;

import java.time.LocalDateTime;

public record ParticipantView(
        String userId,
        MemberRole role,
        boolean active,
        LocalDateTime joinedAt,
        LocalDateTime leftAt
) {
    public static ParticipantView from(SessionUserEntity entity) {
        return new ParticipantView(
                entity.getUserId(),
                entity.getRole(),
                entity.isActive(),
                entity.getJoinedAt(),
                entity.getLeftAt()
        );
    }
}
