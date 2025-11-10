package com.cloud.jml.exception.orders;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OrdenRuntimeException extends RuntimeException {

    private final HttpStatus status;

    public OrdenRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public OrdenRuntimeException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
