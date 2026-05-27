package com.chat.domain.exception;

public class CdlException extends RuntimeException {

    private final String code;
    private final String defaultMessage;
    private final String detail;

    public CdlException(CdlExceptionCode exceptionCode) {
        this(exceptionCode, null);
    }

    public CdlException(CdlExceptionCode exceptionCode, String detail) {
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
