package com.chat.websocket.broadcast;

import com.chat.domain.common.IdGenerator;
import com.chat.websocket.dto.ChatMessageResponse;

import java.time.Instant;

public record DistributedMessage(
        String id,
        String serverId,
        String sessionId,
        String excludeServerId,
        Instant timestamp,
        ChatMessageResponse payload
) {
    public static DistributedMessage create(
            String serverId,
            String sessionId,
            ChatMessageResponse payload
    ) {
        return new DistributedMessage(
                IdGenerator.generate(),
                serverId,
                sessionId,
                serverId,
                Instant.now(),
                payload
        );
    }
}
