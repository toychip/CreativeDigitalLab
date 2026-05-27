package com.chat.websocket.dto;

public record SendMessageRequest(
    String sessionId,
    String clientEventId,
    String content
) {
}
