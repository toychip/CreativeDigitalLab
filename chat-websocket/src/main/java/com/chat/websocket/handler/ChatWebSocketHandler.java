package com.chat.websocket.handler;

import com.chat.application.service.ChatEventService;
import com.chat.application.service.command.MessageCommand;
import com.chat.domain.exception.CdlException;
import com.chat.websocket.broadcast.RedisBroadcastSubscriber;
import com.chat.websocket.dto.ErrorMessage;
import com.chat.websocket.dto.InboundMessageType;
import com.chat.websocket.exception.WebSocketExceptionCode;
import com.chat.websocket.registry.WebSocketSessionRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.EOFException;

@Slf4j
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private final WebSocketSessionRegistry registry;
    private final ChatEventService chatEventService;
    private final RedisBroadcastSubscriber subscriber;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(
            WebSocketSessionRegistry registry,
            ChatEventService chatEventService,
            RedisBroadcastSubscriber subscriber,
            @Qualifier("distributedObjectMapper") ObjectMapper objectMapper
    ) {
        this.registry = registry;
        this.chatEventService = chatEventService;
        this.subscriber = subscriber;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session);
        if (userId == null) return;

        registry.addSession(userId, session);
        log.info("Session established for userId={}", userId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (!(message instanceof TextMessage textMessage)) {
            log.warn("Unsupported message type: {}", message.getClass().getName());
            return;
        }
        String userId = getUserId(session);
        if (userId == null) return;

        String clientEventId = null;
        try {
            JsonNode root = objectMapper.readTree(textMessage.getPayload());
            String typeStr = root.path("type").asText(null);
            clientEventId = root.path("clientEventId").asText(null);

            InboundMessageType type = InboundMessageType.parseType(typeStr);
            switch (type) {
                case SEND_MESSAGE -> handleSendMessage(userId, root);
                case EDIT_MESSAGE -> handleEditMessage(userId, root);
                case DELETE_MESSAGE -> handleDeleteMessage(userId, root);
            }
        } catch (CdlException e) {
            log.warn("CdlException: code={}, detail={}", e.code(), e.detail());
            sendError(session, e, clientEventId);
        } catch (Exception e) {
            log.error("Failed to handle message", e);
            sendError(session, new CdlException(WebSocketExceptionCode.INVALID_MESSAGE_FORMAT), clientEventId);
        }
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
        cleanupIfNoSessions();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String userId = getUserId(session);
        if (userId != null) {
            registry.removeSession(userId, session);
            log.info("Session removed for userId={}", userId);
        }
        cleanupIfNoSessions();
    }

    private void cleanupIfNoSessions() {
        if (!registry.hasAnyOpenSession()) {
            subscriber.unsubscribeAll();
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleSendMessage(String userId, JsonNode root) {
        String sessionId = requireSessionId(root);
        String clientEventId = requireClientEventId(root);
        String content = root.path("content").asText(null);

        chatEventService.appendMessage(MessageCommand.sent(sessionId, clientEventId, userId, content));
    }

    private void handleEditMessage(String userId, JsonNode root) {
        String sessionId = requireSessionId(root);
        String clientEventId = requireClientEventId(root);
        String messageId = requireMessageId(root);
        String content = root.path("content").asText(null);

        chatEventService.appendMessage(MessageCommand.edited(sessionId, clientEventId, userId, messageId, content));
    }

    private void handleDeleteMessage(String userId, JsonNode root) {
        String sessionId = requireSessionId(root);
        String clientEventId = requireClientEventId(root);
        String messageId = requireMessageId(root);

        chatEventService.appendMessage(MessageCommand.deleted(sessionId, clientEventId, userId, messageId));
    }

    private String requireSessionId(JsonNode root) {
        String sessionId = root.path("sessionId").asText(null);
        if (sessionId == null || sessionId.isBlank()) {
            throw new CdlException(WebSocketExceptionCode.SESSION_ID_REQUIRED);
        }
        return sessionId;
    }

    private String requireClientEventId(JsonNode root) {
        String clientEventId = root.path("clientEventId").asText(null);
        if (clientEventId == null || clientEventId.isBlank()) {
            throw new CdlException(WebSocketExceptionCode.CLIENT_EVENT_ID_REQUIRED);
        }
        return clientEventId;
    }

    private String requireMessageId(JsonNode root) {
        String messageId = root.path("messageId").asText(null);
        if (messageId == null || messageId.isBlank()) {
            throw new CdlException(WebSocketExceptionCode.MESSAGE_ID_REQUIRED);
        }
        return messageId;
    }

    private void sendError(WebSocketSession session, CdlException e, String clientEventId) {
        try {
            ErrorMessage error = ErrorMessage.from(e, clientEventId);
            String json = objectMapper.writeValueAsString(error);
            session.sendMessage(new TextMessage(json));
        } catch (Exception ex) {
            log.error("Failed to send error message", ex);
        }
    }

    private String getUserId(WebSocketSession session) {
        Object value = session.getAttributes().get(SessionHandshakeInterceptor.USER_ID_ATTRIBUTE);
        if (value instanceof String s) return s;
        return null;
    }
}
