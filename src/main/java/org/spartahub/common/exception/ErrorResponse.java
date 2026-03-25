package org.spartahub.common.exception;

import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String error,
        Object message,
        String field,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(HttpStatusCode status, Object message) {
        return new ErrorResponse(
                status.value(),
                status.toString(),
                message,
                null,
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(HttpStatusCode status, String field, Object message) {
        return new ErrorResponse(
                status.value(),
                status.toString(),
                message,
                field,
                LocalDateTime.now()
        );
    }
}