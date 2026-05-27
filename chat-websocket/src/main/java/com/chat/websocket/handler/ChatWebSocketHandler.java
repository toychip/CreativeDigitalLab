package com.chat.websocket.handler;

import com.chat.websocket.registry.WebSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.EOFException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final WebSocketSessionRegistry registry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session);
        if (userId == null) {
            return;
        }
        registry.addSession(userId, session);
        log.info("Session established for userId={}", userId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String userId = getUserId(session);
        if (exception instanceof EOFException) {
            log.debug("WebSocket closed by client. userId={}", userId);
        } else {
            log.error("WebSocket transport error. userId={}", userId, exception);
        }
        if (userId != null) {
            registry.removeSession(userId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String userId = getUserId(session);
        if (userId != null) {
            registry.removeSession(userId, session);
            log.info("Session removed for userId={}", userId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String getUserId(WebSocketSession session) {
        Object value = session.getAttributes().get(SessionHandshakeInterceptor.USER_ID_ATTRIBUTE);
        return value instanceof String s ? s : null;
    }
}
