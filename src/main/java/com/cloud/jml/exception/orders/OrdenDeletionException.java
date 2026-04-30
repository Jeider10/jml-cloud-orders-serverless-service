package com.cloud.jml.exception.orders;

import org.springframework.http.HttpStatus;

public class OrdenDeletionException extends OrdenRuntimeException {

    public OrdenDeletionException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "🗑️ [ELIMINACION] " + message);
    }

    public OrdenDeletionException(String message, Throwable cause) {
        super(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "🗑️ [ELIMINACION] " + message +
                        (cause != null ? " | 💥 Causa: " + cause.getMessage() : "")
        );
    }

    // Violacion de integridad referencial (por constraints o dependencias)
    public static OrdenDeletionException integrityViolation(Throwable cause) {
        return new OrdenDeletionException(
                "❌ [INTEGRIDAD] No se pudo eliminar la orden debido a una violacion de integridad referencial.",
                cause
        );
    }

    // Error de acceso a datos
    public static OrdenDeletionException dataAccessError(Throwable cause) {
        return new OrdenDeletionException(
                "❌ [DATOS] Error de acceso a la base de datos al intentar eliminar la orden.",
                cause
        );
    }

    // Error inesperado
    public static OrdenDeletionException unexpected(Throwable cause) {
        return new OrdenDeletionException(
                "💥 [INESPERADO] Ocurrio un error inesperado al intentar eliminar la orden.",
                cause
        );
    }
}
