package com.chat.domain.exception;

public class CdlException extends RuntimeException {

    private final ExceptionCode code;
    private final String detail;

    public CdlException(ExceptionCode code) {
        this(code, null);
    }

    public CdlException(ExceptionCode code, String detail) {
        super(detail != null ? "[" + code + "] " + detail : "[" + code + "] " + code.defaultMessage());
        this.code = code;
        this.detail = detail;
    }

    public ExceptionCode code() {
        return code;
    }

    public Integer statusCode() {
        return code.statusCode();
    }

    public String defaultMessage() {
        return code.defaultMessage();
    }

    public String detail() {
        return detail;
    }
}
