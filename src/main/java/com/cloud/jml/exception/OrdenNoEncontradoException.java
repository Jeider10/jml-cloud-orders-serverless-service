package com.cloud.jml.exception;

public class OrdenNoEncontradoException extends RuntimeException {

    public OrdenNoEncontradoException(Long codigo) {
        super("No se encontró orden con código: " + codigo);
    }
}
