package com.cloud.jml.exception.producto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ProductoRuntimeException extends RuntimeException {

    private final HttpStatus status;

    protected ProductoRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected ProductoRuntimeException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
