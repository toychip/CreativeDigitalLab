package com.chat.application.service.command;

import com.chat.domain.session.MessageStatus;

public record MessageCommand(
    String sessionId,
    String clientEventId,
    String senderId,
    String messageId,
    String content,
    MessageStatus type
) {
}
