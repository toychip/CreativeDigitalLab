package com.chat.api.dto;

/** join / leave 공통 요청 */
public record UserSessionRequest(String userId, String clientEventId) {
}
