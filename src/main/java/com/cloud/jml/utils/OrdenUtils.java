package com.cloud.jml.utils;

import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.model.OrdenEntity;
import com.cloud.jml.repository.OrdenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Component // 🔹 Anotación para indicar que es un componente de Spring
public class OrdenUtils {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("d/M/yyyy, h:mm:ss a", Locale.of("es", "CO"));

    private final OrdenRepository ordenRepository;

    public OrdenUtils(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
        log.info("🔥 OrdenUtils inicializado correctamente.");
    }

    public String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) return null;

        String fechaFormateada = fecha.format(FORMATTER).toLowerCase();
        fechaFormateada = fechaFormateada.replace("a. m.", "a.m.").replace("p. m.", "p.m.");
        return fechaFormateada;
    }

    public void asignarFechasFormateadas(OrdenEntity ordenEntity, OrdenResponseDTO ordenResponseDTO) {
        if (ordenEntity.getFechaCreacion() != null) {
            ordenResponseDTO.setFechaCreacion(formatearFecha(ordenEntity.getFechaCreacion()));
        } else {
            ordenResponseDTO.setFechaCreacion(null);
        }

        if (ordenEntity.getFechaActualizacion() != null) {
            ordenResponseDTO.setFechaActualizacion(formatearFecha(ordenEntity.getFechaActualizacion()));
        } else {
            ordenResponseDTO.setFechaActualizacion(null);
        }
    }
}
