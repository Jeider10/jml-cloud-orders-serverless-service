package com.cloud.jml.exception;

import com.cloud.jml.exception.cantidad.CantidadInvalidaException;
import com.cloud.jml.exception.orders.OrdenRuntimeException;
import com.cloud.jml.exception.producto.ProductoRuntimeException;
import com.cloud.jml.exception.stock.StockInsuficienteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrdenRuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleOrdersErrors(OrdenRuntimeException ex) {
        return buildErrorResponse(ex.getStatus(), "📦 Error en orden", ex.getMessage());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsuficiente(StockInsuficienteException ex) {
        return buildErrorResponse(ex.getStatus(), "📦 Stock insuficiente", ex.getMessage());
    }

    @ExceptionHandler(CantidadInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleCantidadInvalida(CantidadInvalidaException ex) {
        return buildErrorResponse(ex.getStatus(), "📦 Cantidad inválida", ex.getMessage());
    }

    @ExceptionHandler(ProductoRuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleProductoErrors(ProductoRuntimeException ex) {
        return buildErrorResponse(ex.getStatus(), "📦 Error en producto", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "🔥 Error interno", ex.getMessage());
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
