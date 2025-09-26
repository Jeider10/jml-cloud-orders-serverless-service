package com.cloud.jml.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

public class GeneralException extends RuntimeException {

    // Constructor privado para evitar la instancia implícita
    public GeneralException() {
        throw new UnsupportedOperationException("Utility class - instantiation not allowed");
    }

    public GeneralException(String message, Throwable cause) {
        super(message, cause);
    }

    public static void handleAuthError(HttpServletResponse response, AuthenticationException authException) throws IOException {
        if (response.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
            buildResponseError(response, HttpServletResponse.SC_NOT_FOUND,
                    "No encontrado", "El recurso solicitado no existe.");
        } else if (response.getStatus() == HttpServletResponse.SC_CONFLICT) {
            buildResponseError(response, HttpServletResponse.SC_CONFLICT,
                    "Conflicto", "La solicitud no pudo completarse debido a un conflicto con el estado actual del recurso.");
        } else if (response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {
            String message = "Credenciales inválidas. Verifique sus datos de sesión.";
            if (authException.getMessage().contains("Bad credentials")) {
                message = "Usuario o contraseña incorrectos. Verifique sus datos de sesión.";
            }
            buildResponseError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "No autorizado", message);
        } else {
            buildResponseError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "No autorizado", "Acceso denegado.");
        }
    }

    public static void buildResponseError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().write(buildErrorMessage(status, error, message));
    }

    public static String buildErrorMessage(int status, String error, String message) {
        return String.format("{\"status\": %d, \"error\": \"%s\", \"message\": \"%s\"}", status, error, message);
    }
}
