package com.cloud.jml.exception.cantidad;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CantidadRuntimeException extends RuntimeException {

    private final HttpStatus status;

    protected CantidadRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected CantidadRuntimeException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
