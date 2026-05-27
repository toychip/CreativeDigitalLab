package com.chat.websocket.broadcast;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RedisBroadcastSubscriber implements MessageListener {

    private static final int MAX_PROCESSED_MESSAGES = 10000;
    private static final int EVICT_BATCH_SIZE = MAX_PROCESSED_MESSAGES / 4;

    private final ObjectMapper objectMapper;
    private final LocalBroadcaster localBroadcaster;
    private final GlobalBroadcaster globalBroadcaster;

    private final Set<String> processedMessages = ConcurrentHashMap.newKeySet();

    public RedisBroadcastSubscriber(
            @Qualifier("distributedObjectMapper") ObjectMapper objectMapper,
            LocalBroadcaster localBroadcaster,
            GlobalBroadcaster globalBroadcaster
    ) {
        this.objectMapper = objectMapper;
        this.localBroadcaster = localBroadcaster;
        this.globalBroadcaster = globalBroadcaster;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            DistributedMessage envelope = objectMapper.readValue(json, DistributedMessage.class);

            // self-echo: 자기 서버가 publish 한 envelope 은 무시
            if (envelope.excludeServerId().equals(globalBroadcaster.getServerId())) {
                return;
            }

            // Redis Pub/Sub 의 같은 envelope 두 번 도착 시 두 번째 skip (Redis Retry)
            // add 가 false = 이미 있음 = 중복
            if (!processedMessages.add(envelope.id())) {
                return;
            }

            localBroadcaster.broadcast(envelope.sessionId(), envelope.payload());

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
}
