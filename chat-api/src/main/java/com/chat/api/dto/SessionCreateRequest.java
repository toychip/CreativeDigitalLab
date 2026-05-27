package com.chat.api.dto;

public record SessionCreateRequest(String creatorUserId, String clientEventId) {
}
