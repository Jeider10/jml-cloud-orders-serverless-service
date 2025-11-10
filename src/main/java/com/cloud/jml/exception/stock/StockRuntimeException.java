package com.cloud.jml.exception.stock;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StockRuntimeException extends RuntimeException {

    private final HttpStatus status;

    public StockRuntimeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public StockRuntimeException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
