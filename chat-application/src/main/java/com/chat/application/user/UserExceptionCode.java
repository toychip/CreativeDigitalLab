package com.chat.application.user;

import com.chat.domain.exception.CdlExceptionCode;

public enum UserExceptionCode implements CdlExceptionCode {
    USERNAME_ALREADY_TAKEN("USER-001-USERNAME_ALREADY_TAKEN", "이미 사용 중인 username 입니다."),
    USER_NOT_FOUND("USER-002-USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");

    private final String code;
    private final String defaultMessage;

    UserExceptionCode(String code, String defaultMessage) {
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
