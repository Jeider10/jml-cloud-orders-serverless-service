package com.cloud.jml.utils;

import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.model.OrdenDetalleEntity;
import com.cloud.jml.model.OrdenEntity;
import com.cloud.jml.repository.OrdenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component // 🔹 Anotación para indicar que es un componente de Spring
public class OrdenUtils {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("d/M/yyyy, h:mm:ss a", Locale.of("es", "CO"));

    private final OrdenRepository ordenRepository;
    private final OrdenMapper mapper;

    public OrdenUtils(OrdenRepository ordenRepository, OrdenMapper mapper) {
        this.ordenRepository = ordenRepository;
        this.mapper = mapper;
        log.info("🔥 OrdenUtils inicializado correctamente.");
    }

    public OrdenEntity crearNuevaOrden(OrdenRequestDTO requestDTO) {
        log.info("🆕 Creando nueva orden para cliente: {}", requestDTO.getIdentificacionCliente());

        OrdenEntity ordenEntity = mapper.mapRequestDtoToEntity(requestDTO);

        // Generar UUID para numeroOrden si no viene (mapper ya lo genera)
        if (ordenEntity.getNumeroOrden() == null || ordenEntity.getNumeroOrden().isBlank()) {
            ordenEntity.setNumeroOrden(UUID.randomUUID().toString());
        }

        mapper.mapEstadoOrden(ordenEntity);

        // asignar fechas de creación en detalles y enlace orden->detalle ya hecho en mapper
        for (var ordenDetalle : ordenEntity.getDetalles()) {
            ordenDetalle.setOrden(ordenEntity);
            ordenDetalle.setFechaCreacion(LocalDateTime.now());
        }

        // 🔹 Calcular total inicial
        recalcularTotalCompra(ordenEntity);

        log.info("🆕 Nueva orden creada: {}", ordenEntity.getNumeroOrden());

        return ordenEntity;
    }

    public void agregarDetallesOrdenExistente(OrdenEntity ordenEntity, OrdenRequestDTO requestDTO) {
        log.info("📌 Agregando detalles a orden existente: {}", ordenEntity.getNumeroOrden());

        for (var detalleDTO : requestDTO.getDetalles()) {
            OrdenDetalleEntity detalleEntity = mapper.mapDetalleRequestToEntity(detalleDTO);
            detalleEntity.setOrden(ordenEntity);
            detalleEntity.setFechaCreacion(LocalDateTime.now());
            ordenEntity.getDetalles().add(detalleEntity);
        }

        mapper.mapDetalleOrderExistente(ordenEntity, requestDTO);

        // 🔹 Recalcular total después de agregar nuevos detalles
        recalcularTotalCompra(ordenEntity);

        log.info("📌 Detalles agregados a orden existente: {}", ordenEntity.getNumeroOrden());
    }

    public void recalcularTotalCompra(OrdenEntity ordenEntity) {
        log.info("📌 Recalculando total de compra para orden: {}", ordenEntity.getNumeroOrden());

        long total = ordenEntity.getDetalles().stream()
                .mapToLong(d -> (d.getCantidad() != null ? d.getCantidad() : 0L) *
                        (d.getPrecio() != null ? d.getPrecio() : 0L))
                .sum();

        ordenEntity.setTotalCompra(total);

        log.info("📌 Total de compra recalculado: {}", total);
    }

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
