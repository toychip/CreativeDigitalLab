package com.chat.websocket.broadcast;

import com.chat.websocket.dto.ChatMessageResponse;
import com.chat.websocket.registry.WebSocketSessionRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalBroadcaster {

    private final WebSocketSessionRegistry registry;
    private final ObjectMapper objectMapper;

    public LocalBroadcaster(
            WebSocketSessionRegistry registry,
            @Qualifier("distributedObjectMapper") ObjectMapper objectMapper
    ) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    public void broadcast(String sessionId, ChatMessageResponse payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for sessionId={}", sessionId, e);
            return;
        }
        registry.connectedUserIds().forEach(userId -> {
            if (isActiveMember(sessionId, userId)) {
                registry.sendToUser(userId, json);
            }
        });
    }

    // TODO SessionMembers Table 조회 후 변경
    private boolean isActiveMember(String sessionId, String userId) {
        return false;
    }
}
