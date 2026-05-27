package com.chat.application.service.command;

import com.chat.domain.common.IdGenerator;
import com.chat.domain.session.MessageStatus;

public record MessageCommand(
    String sessionId,
    String clientEventId,
    String senderId,
    String messageId,
    String content,
    MessageStatus type
) {
    public static MessageCommand sent(
        String sessionId,
        String clientEventId,
        String senderId,
        String content
    ) {
        return new MessageCommand(
            sessionId,
            clientEventId,
            senderId,
            IdGenerator.generate(),
            content,
            MessageStatus.SENT
        );
    }

    public static MessageCommand edited(
        String sessionId,
        String clientEventId,
        String senderId,
        String messageId,
        String content
    ) {
        return new MessageCommand(
            sessionId,
            clientEventId,
            senderId,
            messageId,
            content,
            MessageStatus.EDITED
        );
    }

    public static MessageCommand deleted(
        String sessionId,
        String clientEventId,
        String senderId,
        String messageId
    ) {
        return new MessageCommand(
            sessionId,
            clientEventId,
            senderId,
            messageId,
            null,
            MessageStatus.DELETED
        );
    }
}
