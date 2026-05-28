package com.chat.api.exception;

public record ExceptionResponse(
        String message,
        String cause,
        long timestamp
) {
    public static ExceptionResponse of(String message, String cause) {
        return new ExceptionResponse(message, cause, System.currentTimeMillis());
    }
}
