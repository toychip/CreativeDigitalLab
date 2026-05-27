package com.chat.websocket.handler;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

// 인증/인가는 과제 비목표. query param {@code ?userId=} 만으로 식별
@Component
public class SessionHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTRIBUTE = "userId";

    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) {
        String userId = resolveUserId(request);
        if (userId == null) {
            return false;
        }
        attributes.put(USER_ID_ATTRIBUTE, userId);
        return true;
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Exception exception
    ) {
    }

    private String resolveUserId(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query == null) {
            return null;
        }
        String userId = parseQuery(query).get("userId");
        return (userId == null || userId.isBlank()) ? null : userId;
    }

    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
            .map(p -> p.split("=", 2))
            .filter(parts -> parts.length == 2)
            .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1], (a, b) -> a));
    }
}
