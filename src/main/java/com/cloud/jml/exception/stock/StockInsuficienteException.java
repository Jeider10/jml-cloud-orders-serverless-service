package com.cloud.jml.exception.stock;

import org.springframework.http.HttpStatus;

public class StockInsuficienteException extends StockRuntimeException {

    public StockInsuficienteException(Long codigoProducto, long cantidadActual, int cantidadARestar) {
        super(
                HttpStatus.BAD_REQUEST,
                "⚠️ [STOCK] Stock insuficiente para el producto " + codigoProducto +
                        " | Disponible: " + cantidadActual +
                        " | Intento de restar: " + cantidadARestar
        );
    }
}
