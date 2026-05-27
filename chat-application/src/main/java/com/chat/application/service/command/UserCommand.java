package com.chat.application.service.command;

import com.chat.domain.event.UserEvent;

public record UserCommand(
    String sessionId,
    String clientEventId,
    String userId,
    UserEvent.Type type
) {
}
