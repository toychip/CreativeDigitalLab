package com.chat.websocket.dto;

import com.chat.domain.exception.CdlException;
import com.chat.domain.exception.ExceptionCode;

public enum InboundMessageType {
    SEND_MESSAGE,
    EDIT_MESSAGE,
    DELETE_MESSAGE

    ;

    public static InboundMessageType parseType(String typeStr) {
        if (typeStr == null) {
            throw new CdlException(ExceptionCode.UNKNOWN_MESSAGE_TYPE);
        }
        try {
            return InboundMessageType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new CdlException(ExceptionCode.UNKNOWN_MESSAGE_TYPE, typeStr);
        }
    }
}
