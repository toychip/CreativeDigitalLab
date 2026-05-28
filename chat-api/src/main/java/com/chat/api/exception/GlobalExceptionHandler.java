package com.chat.api.exception;

import com.chat.domain.exception.CdlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int INTERNAL_SERVER_ERROR = 500;

    @ExceptionHandler(CdlException.class)
    public ResponseEntity<ExceptionResponse> handleCdlException(CdlException e) {
        int status = e.statusCode() != null ? e.statusCode() : INTERNAL_SERVER_ERROR;
        if (status >= INTERNAL_SERVER_ERROR) {
            log.error("CdlException ({}) code={}", status, e.code(), e);
        } else {
            log.warn("CdlException ({}) code={}, message={}", status, e.code(), e.getMessage());
        }
        return ResponseEntity.status(status)
                .body(ExceptionResponse.of(e.defaultMessage(), e.detail()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidation(MethodArgumentNotValidException e) {
        String cause = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation 실패 - {}", cause);
        return ResponseEntity.badRequest()
                .body(ExceptionResponse.of("요청 형식이 잘못되었습니다.", cause));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e) {
        log.error("예기치 못한 에러 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.of("예기치 못한 에러가 발생했습니다.", null));
    }
}
