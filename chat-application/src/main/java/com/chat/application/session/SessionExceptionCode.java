package com.chat.application.session;

import com.chat.domain.exception.CdlExceptionCode;

public enum SessionExceptionCode implements CdlExceptionCode {
    SESSION_NOT_FOUND("SESSION-001-SESSION_NOT_FOUND", "세션을 찾을 수 없습니다."),
    SESSION_ALREADY_ENDED("SESSION-002-SESSION_ALREADY_ENDED", "이미 종료된 세션입니다.");

    private final String code;
    private final String defaultMessage;

    SessionExceptionCode(String code, String defaultMessage) {
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
