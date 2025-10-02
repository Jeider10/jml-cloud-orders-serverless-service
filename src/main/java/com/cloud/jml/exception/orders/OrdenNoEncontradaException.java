package com.cloud.jml.exception.orders;

import org.springframework.http.HttpStatus;

public class OrdenNoEncontradaException extends OrdenRuntimeException {

    public OrdenNoEncontradaException(String numeroOrden) {
        super(HttpStatus.NOT_FOUND, "❌ Orden no encontrada: " + numeroOrden);
    }
}
