package com.chat.websocket.registry;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
}
