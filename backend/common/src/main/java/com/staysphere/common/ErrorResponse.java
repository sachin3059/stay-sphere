package com.staysphere.common;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private boolean success;
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public static ErrorResponse of(int status,
                                   String error,
                                   String message,
                                   String path) {
        return ErrorResponse.builder()
                .success(false)
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}