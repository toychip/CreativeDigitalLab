package com.chat.application.message;

public interface MessageQueryService {

    MessagePageResponse getMessagesByCursor(String sessionId, Long cursor, int limit, MessageDirection direction);
}
