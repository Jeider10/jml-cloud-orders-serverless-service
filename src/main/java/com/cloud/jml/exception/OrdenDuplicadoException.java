package com.cloud.jml.exception;

public class OrdenDuplicadoException extends RuntimeException {

    public OrdenDuplicadoException(Long codigo) {
        super("El orden con codigo " + codigo + " ya existe.");
    }
}
