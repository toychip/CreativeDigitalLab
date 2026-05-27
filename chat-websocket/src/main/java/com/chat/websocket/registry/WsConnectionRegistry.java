package com.chat.websocket.registry;

import com.chat.websocket.broadcast.RedisMessageBroker;
import com.chat.websocket.dto.ChatMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WsConnectionRegistry {

    // Redis Set 키 prefix. Set 단위 = serverId 1개, 원소 = 이 서버가 구독 중인 sessionId 들
    // 이 서버 인스턴스의 구독 목록
    // joinSession() sessionId 추가, 마지막 ws connection 종료 시 일괄 unsubscribe
    private static final String SERVER_SESSIONS_KEY_PREFIX = "chat:server:sessions:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisMessageBroker redisMessageBroker;

    private final Map<String, Set<WebSocketSession>> userWsConnections = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        redisMessageBroker.setLocalMessageHandler(this::sendMessageToLocalSession);
    }

    public void addWsConnection(String userId, WebSocketSession wsConnection) {
        log.info("Adding ws connection for userId={}", userId);
        userWsConnections.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(wsConnection);
    }

    public void removeWsConnection(String userId, WebSocketSession wsConnection) {
        Set<WebSocketSession> connections = userWsConnections.get(userId);
        if (connections == null) return;
        connections.remove(wsConnection);

        if (!connections.isEmpty()) return;
        userWsConnections.remove(userId);

        long totalOpenConnections = userWsConnections.values().stream()
                .flatMap(Set::stream)
                .filter(WebSocketSession::isOpen)
                .count();
        if (totalOpenConnections > 0) {
            return;
        }

        String serverSessionsKey = SERVER_SESSIONS_KEY_PREFIX + redisMessageBroker.getServerId();
        Set<String> subscribedSessionIds = redisTemplate.opsForSet().members(serverSessionsKey);
        if (subscribedSessionIds != null) {
            subscribedSessionIds.forEach(redisMessageBroker::unsubscribe);
        }
        redisTemplate.delete(serverSessionsKey);
    }

    public void joinSession(String userId, String sessionId) {
        String serverSessionsKey = SERVER_SESSIONS_KEY_PREFIX + redisMessageBroker.getServerId();
        boolean wasAlreadySubscribed = Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(serverSessionsKey, sessionId)
        );
        if (!wasAlreadySubscribed) {
            redisMessageBroker.subscribe(sessionId);
        }
        redisTemplate.opsForSet().add(serverSessionsKey, sessionId);
        log.info("Joined sessionId={} for userId={}", sessionId, userId);
    }

    public void sendMessageToLocalSession(String sessionId, ChatMessageResponse payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for sessionId={}", sessionId, e);
            return;
        }
        userWsConnections.forEach((userId, connections) -> {
            if (!isActiveMember(sessionId, userId)) {
                return;
            }
            Set<WebSocketSession> closedConnections = new HashSet<>();
            connections.forEach(ws -> {
                if (!ws.isOpen()) {
                    closedConnections.add(ws);
                    return;
                }
                try {
                    ws.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.warn("Failed to send to userId={}", userId, e);
                    closedConnections.add(ws);
                }
            });
            connections.removeAll(closedConnections);
        });
    }

    public boolean isUserOnlineLocally(String userId) {
        Set<WebSocketSession> connections = userWsConnections.get(userId);
        if (connections == null) {
            return false;
        }
        connections.removeIf(ws -> !ws.isOpen());
        if (connections.isEmpty()) {
            userWsConnections.remove(userId);
            return false;
        }
        return true;
    }

    // TODO SessionMember 프로젝션 도입 시 실제 조회로 교체. 현재 목: 모든 사용자를 활성 멤버로 간주.
    private boolean isActiveMember(String sessionId, String userId) {
        return true;
    }
}
