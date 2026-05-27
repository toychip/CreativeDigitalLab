package com.chat.websocket.broadcast;

import com.chat.domain.common.IdGenerator;
import com.chat.websocket.dto.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalBroadcaster {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Getter
    private String serverId;

    @PostConstruct
    public void init() {
        this.serverId = "server-" + IdGenerator.generate();
    }

    public GlobalBroadcaster(
            RedisTemplate<String, String> redisTemplate,
            @Qualifier("distributedObjectMapper") ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void broadcast(String sessionId, ChatMessageResponse payload) {
        try {
            DistributedMessage message = DistributedMessage.create(serverId, sessionId, payload);
            String json = objectMapper.writeValueAsString(message);
            String sessionKey = channel(sessionId);
            redisTemplate.convertAndSend(sessionKey, json);
            log.info("Broadcast to sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("Failed to broadcast to sessionId={}", sessionId, e);
        }
    }

    private String channel(String sessionId) {
        return "chat.session." + sessionId;
    }
}
