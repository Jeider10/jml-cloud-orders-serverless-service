package com.cloud.jml.exception.orders;

import org.springframework.http.HttpStatus;

public class OrdenDeletionException extends OrdenRuntimeException {

    public OrdenDeletionException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "🗑️ " + message);
    }

    public OrdenDeletionException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "🗑️ " + message + " | Causa: " + cause.getMessage());
    }

    // 🔒 Error por violación de integridad (constraint, duplicado, etc.) al eliminar
    public static OrdenDeletionException integrityViolation(Throwable cause) {
        return new OrdenDeletionException("❌ No se pudo eliminar la orden debido a una violación de integridad referencial", cause);
    }

    // ⚙️ Error al acceder o comunicarse con la base de datos al eliminar
    public static OrdenDeletionException dataAccessError(Throwable cause) {
        return new OrdenDeletionException("❌ Error de acceso a datos al intentar eliminar la orden", cause);
    }

    // 💥 Error inesperado (no contemplado en los anteriores) al eliminar
    public static OrdenDeletionException unexpected(Throwable cause) {
        return new OrdenDeletionException("❌ Error inesperado al intentar eliminar la orden", cause);
    }
}
