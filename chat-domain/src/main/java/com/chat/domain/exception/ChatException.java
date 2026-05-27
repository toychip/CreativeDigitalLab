package com.chat.domain.exception;

public class ChatException extends RuntimeException {

    private final String code;
    private final String defaultMessage;
    private final String detail;

    public ChatException(ExceptionCode exceptionCode) {
        this(exceptionCode, null);
    }

    public ChatException(ExceptionCode exceptionCode, String detail) {
        super(detail != null ? detail : exceptionCode.defaultMessage());
        this.code = exceptionCode.code();
        this.defaultMessage = exceptionCode.defaultMessage();
        this.detail = detail;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    public String detail() {
        return detail;
    }
}
