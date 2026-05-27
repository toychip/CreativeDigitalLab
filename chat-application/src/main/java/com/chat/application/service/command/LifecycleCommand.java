package com.chat.application.service.command;

import com.chat.domain.session.SessionStatus;

public record LifecycleCommand(
    String sessionId,
    String clientEventId,
    SessionStatus status
) {
}
