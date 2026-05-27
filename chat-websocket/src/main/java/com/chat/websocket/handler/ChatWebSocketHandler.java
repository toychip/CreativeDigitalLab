package com.chat.websocket.handler;

import com.chat.application.service.ChatEventService;
import com.chat.application.service.command.MessageCommand;
import com.chat.application.sessionuser.SessionUserEntity;
import com.chat.application.sessionuser.SessionUserRepository;
import com.chat.application.user.UserRepository;
import com.chat.domain.exception.CdlException;
import com.chat.websocket.dto.ErrorMessage;
import com.chat.websocket.dto.InboundMessageType;
import com.chat.websocket.exception.WebSocketExceptionCode;
import com.chat.websocket.registry.WsConnectionRegistry;
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
import java.util.List;

@Slf4j
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private final WsConnectionRegistry registry;
    private final ChatEventService chatEventService;
    private final SessionUserRepository sessionUserRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(
            WsConnectionRegistry registry,
            ChatEventService chatEventService,
            SessionUserRepository sessionUserRepository,
            UserRepository userRepository,
            @Qualifier("distributedObjectMapper") ObjectMapper objectMapper
    ) {
        this.registry = registry;
        this.chatEventService = chatEventService;
        this.sessionUserRepository = sessionUserRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsConnection) {
        String userId = getUserId(wsConnection);
        if (userId == null) return;

        registry.addWsConnection(userId, wsConnection);
        log.info("Connection established for userId={}", userId);
        try {
            subscribeActiveSessions(userId);
        } catch (Exception e) {
            log.error("Error while loading active sessions for userId={}", userId, e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession wsConnection, WebSocketMessage<?> message) {
        if (!(message instanceof TextMessage textMessage)) {
            log.warn("Unsupported message type: {}", message.getClass().getName());
            return;
        }
        String userId = getUserId(wsConnection);
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
            touchLastSeen(userId);
        } catch (CdlException e) {
            log.warn("CdlException: code={}, detail={}", e.code(), e.detail());
            sendError(wsConnection, e, clientEventId);
        } catch (Exception e) {
            log.error("Failed to handle message", e);
            sendError(wsConnection, new CdlException(WebSocketExceptionCode.INVALID_MESSAGE_FORMAT), clientEventId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession wsConnection, Throwable exception) {
        String userId = getUserId(wsConnection);
        if (exception instanceof EOFException) {
            log.debug("WebSocket closed by client. userId={}", userId);
        } else {
            log.error("WebSocket transport error. userId={}", userId, exception);
        }
        if (userId != null) {
            registry.removeWsConnection(userId, wsConnection);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wsConnection, CloseStatus closeStatus) {
        String userId = getUserId(wsConnection);
        if (userId != null) {
            registry.removeWsConnection(userId, wsConnection);
            touchLastSeen(userId);
            log.info("Connection removed for userId={}", userId);
        }
    }

    private void subscribeActiveSessions(String userId) {
        try {
            List<String> activeSessionIds = sessionUserRepository.findByUserIdAndIsActiveTrue(userId).stream()
                    .map(SessionUserEntity::getSessionId)
                    .toList();
            activeSessionIds.forEach(sessionId -> registry.joinSession(userId, sessionId));
            log.info("Loaded {} active sessions for userId={}", activeSessionIds.size(), userId);
        } catch (Exception e) {
            log.error("Failed to load active sessions for userId={}", userId, e);
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

    private void sendError(WebSocketSession wsConnection, CdlException e, String clientEventId) {
        try {
            ErrorMessage error = ErrorMessage.from(e, clientEventId);
            String json = objectMapper.writeValueAsString(error);
            wsConnection.sendMessage(new TextMessage(json));
        } catch (Exception ex) {
            log.error("Failed to send error message", ex);
        }
    }

    private String getUserId(WebSocketSession wsConnection) {
        Object value = wsConnection.getAttributes().get(SessionHandshakeInterceptor.USER_ID_ATTRIBUTE);
        if (value instanceof String s) return s;
        return null;
    }

    private void touchLastSeen(String userId) {
        try {
            userRepository.findById(userId).ifPresent(user -> {
                user.touchLastSeen();
                userRepository.save(user);
            });
        } catch (Exception e) {
            log.warn("Failed to touch lastSeenAt for userId={}", userId, e);
        }
    }
}
