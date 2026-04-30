package com.cloud.jml.exception;

import com.cloud.jml.exception.cantidad.CantidadInvalidaException;
import com.cloud.jml.exception.orders.OrdenRuntimeException;
import com.cloud.jml.exception.producto.ProductoRuntimeException;
import com.cloud.jml.exception.stock.StockInsuficienteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura errores de validacion de DTOs (@Valid) @NotBlank, @NotNull, @Email, @Size, etc. y retorna un 400 con los detalles
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "📋 [VALIDACION] Error de validacion en los datos enviados",
                errores);
    }

    // 🧾 Errores de ordenes
    @ExceptionHandler(OrdenRuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleOrdenErrors(OrdenRuntimeException ex) {
        return buildErrorResponse(
                ex.getStatus(),
                "🧾 [ORDEN] Error en orden",
                ex.getMessage());
    }

    // ⚖️ Errores de stock insuficiente
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsuficiente(StockInsuficienteException ex) {
        return buildErrorResponse(
                ex.getStatus(),
                "⚖️ [STOCK] Stock insuficiente",
                ex.getMessage());
    }

    // 🔢 Errores de cantidad invalida
    @ExceptionHandler(CantidadInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleCantidadInvalida(CantidadInvalidaException ex) {
        return buildErrorResponse(
                ex.getStatus(),
                "🔢 [CANTIDAD] Valor de cantidad invalido",
                ex.getMessage());
    }

    // 📦 Errores relacionados con productos
    @ExceptionHandler(ProductoRuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleProductoErrors(ProductoRuntimeException ex) {
        return buildErrorResponse(
                ex.getStatus(),
                "📦 [PRODUCTO] Error en producto",
                ex.getMessage());
    }

    // 🔥 Errores generales no controlados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "🔥 [GENERAL] Error interno del servidor",
                ex.getMessage());
    }

    // 🧱 Metodo comun de respuesta
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}
