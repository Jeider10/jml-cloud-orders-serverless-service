package com.cloud.jml.exception.orders;

import org.springframework.http.HttpStatus;

public class OrdenPersistenceException extends OrdenRuntimeException {

    public OrdenPersistenceException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "💾 [PERSISTENCIA] " + message);
    }

    public OrdenPersistenceException(String message, Throwable cause) {
        super(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "💾 [PERSISTENCIA] " + message +
                        (cause != null ? " | 💥 Causa: " + cause.getMessage() : "")
        );
    }

    // 🔒 Violación de integridad (constraint, duplicado, etc.) al guardar
    public static OrdenPersistenceException integrityViolation(Throwable cause) {
        return new OrdenPersistenceException(
                "❌ [INTEGRIDAD] Violación de integridad referencial al guardar la orden.",
                cause
        );
    }

    // ⚙️ Error técnico de acceso a datos
    public static OrdenPersistenceException dataAccessError(Throwable cause) {
        return new OrdenPersistenceException(
                "⚙️ [DATOS] Error de acceso a la base de datos al intentar guardar la orden.",
                cause
        );
    }

    // 💥 Error inesperado
    public static OrdenPersistenceException unexpected(Throwable cause) {
        return new OrdenPersistenceException(
                "💥 [INESPERADO] Error inesperado al intentar registrar la orden.",
                cause
        );
    }
}
