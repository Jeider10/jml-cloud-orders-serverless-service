package com.cloud.jml.exception.producto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProductoRuntimeException extends RuntimeException {

    private final HttpStatus status;

    public ProductoRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ProductoRuntimeException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
