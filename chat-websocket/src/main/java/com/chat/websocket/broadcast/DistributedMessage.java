package com.chat.websocket.broadcast;

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
}
