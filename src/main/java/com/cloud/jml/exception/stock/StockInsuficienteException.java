package com.cloud.jml.exception.stock;

import org.springframework.http.HttpStatus;

public class StockInsuficienteException extends StockRuntimeException {

    public StockInsuficienteException(Long codigoProducto, long cantidadActual, int cantidadARestar) {
        super(HttpStatus.BAD_REQUEST, "⚠️ Stock insuficiente en la orden. "
                + "producto=" + codigoProducto
                + ", cantidadActual=" + cantidadActual
                + ", intentoRestar=" + cantidadARestar
        );
    }
}