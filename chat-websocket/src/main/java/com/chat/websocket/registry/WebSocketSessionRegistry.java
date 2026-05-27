package com.chat.websocket.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionRegistry {

    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void addSession(String userId, WebSocketSession session) {
        log.info("Adding session for userId={}", userId);
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(String userId, WebSocketSession session) {
        userSessions.compute(userId, (k, sessions) -> {
            if (sessions == null) {
                return null;
            }
            sessions.remove(session);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    public Set<WebSocketSession> getSessions(String userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) {
            return Collections.emptySet();
        }
        return sessions;
    }

    public Set<String> connectedUserIds() {
        return Collections.unmodifiableSet(userSessions.keySet());
    }
}
