package com.chat.websocket.broadcast;

import com.chat.domain.common.IdGenerator;
import com.chat.websocket.dto.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class RedisMessageBroker implements MessageListener {

    private static final int MAX_PROCESSED_MESSAGES = 10000;
    private static final int EVICT_BATCH_SIZE = MAX_PROCESSED_MESSAGES / 4;

    // 사용: broadcast() publish, subscribe()/unsubscribe() listener 등록/해제
    private static final String SESSION_CHANNEL_PREFIX = "chat.session.";

    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer container;
    private final RedisTemplate<String, String> redisTemplate;

    private String serverId;
    private BiConsumer<String, ChatMessageResponse> localMessageHandler;

    private final Set<String> processedMessages = ConcurrentHashMap.newKeySet();
    private final Set<String> subscribedSessions = ConcurrentHashMap.newKeySet();

    public RedisMessageBroker(
            @Qualifier("distributedObjectMapper") ObjectMapper objectMapper,
            RedisMessageListenerContainer container,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.objectMapper = objectMapper;
        this.container = container;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        this.serverId = "server-" + IdGenerator.generate();
    }

    public void setLocalMessageHandler(BiConsumer<String, ChatMessageResponse> handler) {
        this.localMessageHandler = handler;
    }

    public void broadcast(String sessionId, ChatMessageResponse payload) {
        try {
            DistributedMessage message = DistributedMessage.create(serverId, sessionId, payload);
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(SESSION_CHANNEL_PREFIX + sessionId, json);
            log.info("Broadcast to sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("Failed to broadcast to sessionId={}", sessionId, e);
        }
    }

    public void subscribe(String sessionId) {
        if (subscribedSessions.add(sessionId)) {
            ChannelTopic topic = new ChannelTopic(SESSION_CHANNEL_PREFIX + sessionId);
            container.addMessageListener(this, topic);
            log.info("Subscribed to sessionId={}", sessionId);
        }
    }

    public void unsubscribe(String sessionId) {
        if (subscribedSessions.remove(sessionId)) {
            ChannelTopic topic = new ChannelTopic(SESSION_CHANNEL_PREFIX + sessionId);
            container.removeMessageListener(this, topic);
            log.info("Unsubscribed from sessionId={}", sessionId);
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            DistributedMessage envelope = objectMapper.readValue(json, DistributedMessage.class);

            if (envelope.excludeServerId().equals(serverId)) {
                return;
            }

            if (!processedMessages.add(envelope.id())) {
                return;
            }

            if (localMessageHandler != null) {
                localMessageHandler.accept(envelope.sessionId(), envelope.payload());
            }

            if (processedMessages.size() > MAX_PROCESSED_MESSAGES) {
                evictOldest();
            }
        } catch (Exception e) {
            log.error("Failed to handle Redis message", e);
        }
    }

    private void evictOldest() {
        processedMessages.stream()
                .sorted()
                .limit(EVICT_BATCH_SIZE)
                .forEach(processedMessages::remove);
    }

    public String getServerId() {
        return serverId;
    }
}
