package com.cloud.jml.exception.cantidad;

import org.springframework.http.HttpStatus;

public class CantidadInvalidaException extends CantidadRuntimeException {

    public CantidadInvalidaException(int cantidadARestar) {
        super(
                HttpStatus.BAD_REQUEST,
                "⚠️ [VALIDACIÓN] Cantidad inválida. La cantidad a restar no puede ser menor o igual a 0. " +
                        "Cantidad solicitada: " + cantidadARestar
        );
    }
}
