package com.cloud.jml.exception.cantidad;

import org.springframework.http.HttpStatus;

public class CantidadInvalidaException extends CantidadRuntimeException {

    public CantidadInvalidaException(int cantidadARestar) {
        super(HttpStatus.BAD_REQUEST, "⚠️ La cantidad a restar no puede ser mayor a la cantidad disponible. Cantidad a restar: " + cantidadARestar);
    }
}
