package com.cloud.jml.exception.orders;

import org.springframework.http.HttpStatus;

public class OrdenPorClienteNoEncontradaException extends OrdenRuntimeException {

    public OrdenPorClienteNoEncontradaException(Long identificacionCliente) {
        super(
                HttpStatus.NOT_FOUND,
                "❌ [CONSULTA] No se encontro ninguna orden asociada al cliente con identificacion: " + identificacionCliente
        );
    }
}
