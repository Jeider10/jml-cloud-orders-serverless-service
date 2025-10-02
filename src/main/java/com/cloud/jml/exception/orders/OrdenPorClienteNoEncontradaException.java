package com.cloud.jml.exception.orders;

import org.springframework.http.HttpStatus;

public class OrdenPorClienteNoEncontradaException extends OrdenRuntimeException {

    public OrdenPorClienteNoEncontradaException(Long identificacionCliente) {
        super(HttpStatus.NOT_FOUND, "❌ Orden no encontrada con identificación de cliente: " + identificacionCliente);
    }
}
