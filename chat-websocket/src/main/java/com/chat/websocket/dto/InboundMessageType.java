package com.chat.websocket.dto;

import com.chat.domain.exception.CdlException;
import com.chat.websocket.exception.WebSocketExceptionCode;

public enum InboundMessageType {
    SEND_MESSAGE,
    EDIT_MESSAGE,
    DELETE_MESSAGE

    ;

    public static InboundMessageType parseType(String typeStr) {
        if (typeStr == null) {
            throw new CdlException(WebSocketExceptionCode.UNKNOWN_MESSAGE_TYPE);
        }
        try {
            return InboundMessageType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new CdlException(WebSocketExceptionCode.UNKNOWN_MESSAGE_TYPE, typeStr);
        }
    }
}
