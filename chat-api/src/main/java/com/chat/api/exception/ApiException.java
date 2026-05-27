package com.chat.api.exception;

import com.chat.domain.exception.CdlExceptionCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, CdlExceptionCode exceptionCode) {
        super(exceptionCode.defaultMessage());
        this.status = status;
        this.code = exceptionCode.code();
    }
}
