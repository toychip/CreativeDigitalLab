package com.chat.websocket.exception;

import com.chat.domain.exception.ExceptionCode;

public enum WebSocketExceptionCode implements ExceptionCode {
    INVALID_MESSAGE_FORMAT("WS-001-INVALID_MESSAGE_FORMAT", "메시지 형식이 올바르지 않습니다."),
    SESSION_ID_REQUIRED("WS-002-SESSION_ID_REQUIRED", "sessionId 가 필요합니다."),
    CLIENT_EVENT_ID_REQUIRED("WS-003-CLIENT_EVENT_ID_REQUIRED", "clientEventId 가 필요합니다."),
    UNKNOWN_MESSAGE_TYPE("WS-004-UNKNOWN_MESSAGE_TYPE", "알 수 없는 메시지 타입입니다."),
    UNAUTHORIZED("WS-005-UNAUTHORIZED", "인증이 필요합니다."),
    NOT_SESSION_MEMBER("WS-006-NOT_SESSION_MEMBER", "세션의 멤버가 아닙니다."),
    SESSION_NOT_FOUND("WS-007-SESSION_NOT_FOUND", "세션을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("WS-008-INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String defaultMessage;

    WebSocketExceptionCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }
}
