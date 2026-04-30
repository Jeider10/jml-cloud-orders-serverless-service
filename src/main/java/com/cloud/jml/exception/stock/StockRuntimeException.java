package com.cloud.jml.exception.stock;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class StockRuntimeException extends RuntimeException {

    private final HttpStatus status;

    protected StockRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected StockRuntimeException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
