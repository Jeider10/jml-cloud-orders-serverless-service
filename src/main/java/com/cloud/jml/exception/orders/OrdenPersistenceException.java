package com.cloud.jml.exception.orders;

import org.springframework.http.HttpStatus;

public class OrdenPersistenceException extends OrdenRuntimeException {

    public OrdenPersistenceException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "💾 " + message);
    }

    public OrdenPersistenceException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "💾 " + message + " | Causa: " + cause.getMessage());
    }

    // 🔒 Error por violación de integridad (constraint, duplicado, etc.) al guardar
    public static OrdenPersistenceException integrityViolation(Throwable cause) {
        return new OrdenPersistenceException("❌ Violación de integridad en base de datos al guardar la orden", cause);
    }

    // ⚙️ Error al acceder o comunicarse con la base de datos al guardar
    public static OrdenPersistenceException dataAccessError(Throwable cause) {
        return new OrdenPersistenceException("❌ Error de acceso a datos al intentar guardar la orden", cause);
    }

    // 💥 Error inesperado (no contemplado en los anteriores) al guardar
    public static OrdenPersistenceException unexpected(Throwable cause) {
        return new OrdenPersistenceException("❌ Error inesperado al registrar la orden", cause);
    }
}
