package com.chat.application.session;

import com.chat.domain.session.ChatSession;
import com.chat.domain.session.Message;
import com.chat.domain.session.MessageStatus;
import com.chat.domain.session.SessionStatus;

import java.util.List;
import java.util.Set;

public record TimelineResponse(
        String sessionId,
        SessionStatus status,
        Set<String> participants,
        List<MessageSummary> messages
) {
    public record MessageSummary(
            String messageId,
            String senderId,
            String content,
            MessageStatus status
    ) {
        public static MessageSummary from(Message m) {
            return new MessageSummary(m.messageId(), m.senderId(), m.content(), m.status());
        }
    }

    public static TimelineResponse from(ChatSession session) {
        List<MessageSummary> messages = session.getMessages().stream()
                .map(MessageSummary::from)
                .toList();
        return new TimelineResponse(
                session.getSessionId(),
                session.getStatus(),
                session.getParticipants(),
                messages
        );
    }
}
