package com.cloud.jml.exception.producto;

import org.springframework.http.HttpStatus;

public class ProductoNoEncontradoException extends ProductoRuntimeException {

    public ProductoNoEncontradoException(Long codigoProducto) {
        super(
                HttpStatus.NOT_FOUND,
                "❌ [CONSULTA] No se encontro ningun producto con el codigo: " + codigoProducto
        );
    }
}
