package com.cloud.jml.exception.cantidad;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CantidadRuntimeException extends RuntimeException {

    private final HttpStatus status;

    public CantidadRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
