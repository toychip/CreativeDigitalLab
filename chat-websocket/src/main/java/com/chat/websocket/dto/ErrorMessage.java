package com.chat.websocket.dto;

import com.chat.domain.exception.CdlException;

public record ErrorMessage(
    String code,
    String message,
    String detail,
    String clientEventId
) {
    public static ErrorMessage from(CdlException e, String clientEventId) {
        return new ErrorMessage(e.code(), e.defaultMessage(), e.detail(), clientEventId);
    }
}
