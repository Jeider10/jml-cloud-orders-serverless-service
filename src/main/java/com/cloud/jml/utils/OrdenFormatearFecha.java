package com.cloud.jml.utils;

import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.model.OrdenEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Component
public class OrdenFormatearFecha {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("d/M/yyyy, h:mm:ss a", Locale.of("es", "CO"));

    public String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return null;
        }

        String fechaFormateada = fecha.format(FORMATTER).toLowerCase();
        log.info("📌 Fecha formateada originalmente: {}", fechaFormateada);

        // Reemplazar y reasignar el valor "a. m." → "a.m." y "p. m." → "p.m."
        fechaFormateada = fechaFormateada
                .replace("a. m.", "a.m.")
                .replace("p. m.", "p.m.");

        log.info("📌 Fecha formateada final: {}", fechaFormateada);

        return fechaFormateada;
    }

    public void asignarFechasFormateadas(OrdenEntity ordenEntity, OrdenResponseDTO ordenResponseDTO) {
        log.info("📌 Asignando fechas formateadas a la respuesta de la orden: {}", ordenEntity.getNumeroOrden());

        if (ordenEntity.getFechaCreacion() != null) {
            String fechaCreacion = formatearFecha(ordenEntity.getFechaCreacion());
            log.info("📌 Fecha creación formateada: {}", fechaCreacion);

            ordenResponseDTO.setFechaCreacion(fechaCreacion);
        } else {
            ordenResponseDTO.setFechaCreacion(null);
        }

        if (ordenEntity.getFechaActualizacion() != null) {
            String fechaActualizacion = formatearFecha(ordenEntity.getFechaActualizacion());
            log.info("📌 Fecha actualización formateada: {}", fechaActualizacion);

            ordenResponseDTO.setFechaActualizacion(fechaActualizacion);
        } else {
            ordenResponseDTO.setFechaActualizacion(null);
        }
    }
}
