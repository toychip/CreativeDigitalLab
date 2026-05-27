package com.chat.websocket.broadcast;

import com.chat.domain.common.IdGenerator;
import com.chat.websocket.dto.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class RedisBroadcastPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Getter
    private final String serverId = resolveServerId();

    public RedisBroadcastPublisher(
            RedisTemplate<String, String> redisTemplate,
            @Qualifier("distributedObjectMapper") ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String sessionId, ChatMessageResponse payload) {
        try {
            DistributedMessage envelope = new DistributedMessage(
                    IdGenerator.generate(),
                    serverId,
                    sessionId,
                    serverId,
                    Instant.now(),
                    payload
            );
            String json = objectMapper.writeValueAsString(envelope);
            redisTemplate.convertAndSend(channel(sessionId), json);
            log.info("Broadcast to sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("Failed to broadcast to sessionId={}", sessionId, e);
        }
    }

    private String channel(String sessionId) {
        return "chat.session." + sessionId;
    }

    private static String resolveServerId() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname == null) {
            throw new IllegalStateException("HOSTNAME 환경 변수가 필요합니다");
        }
        return hostname;
    }
}
