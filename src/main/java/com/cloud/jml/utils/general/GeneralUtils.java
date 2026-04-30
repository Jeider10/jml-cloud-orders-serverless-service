package com.cloud.jml.utils.general;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component // 🔹 Anotacion para indicar que es un componente de Spring
public class GeneralUtils {

    /**
     * 🧩 Obtiene una variable de entorno o usa un valor por defecto.
     * Soporta String, Integer, Boolean y Long.
     */
    @SuppressWarnings("unchecked")
    public <T> T getEnvOrDefault(String envVar, T defaultValue) {
        String envValue = System.getenv(envVar);

        if (envValue == null || envValue.isBlank()) {
            log.warn("⚠️ Variable de entorno {} no definida, usando valor por defecto: {}", envVar, defaultValue);
            return defaultValue;
        }

        log.info("🌎 Variable de entorno {} detectada con valor: {}", envVar, envValue);

        try {
            return switch (defaultValue) {
                case Integer i -> (T) Integer.valueOf(envValue);
                case Long l -> (T) Long.valueOf(envValue);
                case Boolean b -> (T) Boolean.valueOf(envValue);
                case null, default ->
                    // Por defecto se trata como String
                        (T) envValue;
            };
        } catch (Exception e) {
            log.error("❌ Error al convertir variable de entorno {}. Usando valor por defecto: {}", envVar, defaultValue, e);
            return defaultValue;
        }
    }
}
