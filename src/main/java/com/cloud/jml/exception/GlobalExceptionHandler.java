package com.cloud.jml.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrdenDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteDuplicado(OrdenDuplicadoException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Cliente duplicado", ex.getMessage());
    }

    @ExceptionHandler(OrdenNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNoEncontrado(OrdenNoEncontradoException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Cliente no encontrado", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
