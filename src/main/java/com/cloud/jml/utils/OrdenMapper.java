package com.cloud.jml.utils;

import com.cloud.jml.dto.OrdenDetalleRequestDTO;
import com.cloud.jml.dto.OrdenDetalleResponseDTO;
import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.model.OrdenDetalleEntity;
import com.cloud.jml.model.OrdenEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class OrdenMapper {

    public static final String ESTADO_ABIERTA = "ABIERTA";

    private final OrdenFormatearFecha ordenFormatearFecha;

    public OrdenMapper(OrdenFormatearFecha ordenFormatearFecha) {
        this.ordenFormatearFecha = ordenFormatearFecha;
        log.info("🔥 OrdenMapper inicializado correctamente.");
    }

    /**
     * 📦 Convierte un DTO de solicitud de orden en una entidad lista para persistir.
     */
    public OrdenEntity mapRequestDtoToEntity(OrdenRequestDTO ordenRequestDTO) {
        log.info("📦 [MAPEO] Iniciando mapeo DTO → Entity para creación de orden.");

        OrdenEntity ordenEntity = new OrdenEntity();

        // generar numeroOrden (UUID)
        String numeroOrden = UUID.randomUUID().toString();
        ordenEntity.setNumeroOrden(numeroOrden);

        ordenEntity.setIdentificacionCliente(ordenRequestDTO.getIdentificacionCliente());
        ordenEntity.setNombreCliente(ordenRequestDTO.getNombreCliente());
        ordenEntity.setIdentificacionEmpleado(ordenRequestDTO.getIdentificacionEmpleado());
        ordenEntity.setNombreEmpleado(ordenRequestDTO.getNombreEmpleado());
        ordenEntity.setIdentificacionProveedor(ordenRequestDTO.getIdentificacionProveedor());
        ordenEntity.setNombreProveedor(ordenRequestDTO.getNombreProveedor());
        ordenEntity.setFechaCreacion(LocalDateTime.now());

        // 📦 Mapeo lista de detalles
        if (ordenRequestDTO.getDetalles() != null && !ordenRequestDTO.getDetalles().isEmpty()) {
            List<OrdenDetalleEntity> detalles = new ArrayList<>();

            for (OrdenDetalleRequestDTO detalleDTO : ordenRequestDTO.getDetalles()) {
                OrdenDetalleEntity detalleEntity = mapDetalleRequestToEntity(detalleDTO);
                detalleEntity.setFechaCreacion(LocalDateTime.now());
                detalleEntity.setOrden(ordenEntity);
                detalles.add(detalleEntity);
            }

            ordenEntity.setDetalles(detalles);
        } else {
            log.warn("⚠️ [VALIDACIÓN] La orden no contiene detalles asociados (numeroOrden={}).", numeroOrden);
        }

        log.info("✅ [MAPEO] Mapeo completado DTO → Entity (numeroOrden={})", numeroOrden);

        return ordenEntity;
    }

    /**
     * 📦 Convierte un detalle de solicitud en su entidad correspondiente.
     */
    public OrdenDetalleEntity mapDetalleRequestToEntity(OrdenDetalleRequestDTO detalleDTO) {
        log.debug("📦 [MAPEO] Mapeando detalle: código={}, producto={}", detalleDTO.getCodigo(), detalleDTO.getProducto());

        OrdenDetalleEntity detalleEntity = new OrdenDetalleEntity();

        detalleEntity.setCodigo(detalleDTO.getCodigo());
        detalleEntity.setProducto(detalleDTO.getProducto());
        detalleEntity.setDescripcion(detalleDTO.getDescripcion());
        detalleEntity.setCantidad(detalleDTO.getCantidad());
        detalleEntity.setPrecio(detalleDTO.getPrecio());

        log.debug("✅ [MAPEO] Detalle mapeado correctamente: código={}, producto={}", detalleDTO.getCodigo(), detalleDTO.getProducto());

        return detalleEntity;
    }

    /**
     * 📦 Convierte una entidad de orden completa en un DTO de respuesta.
     */
    public OrdenResponseDTO mapEntityToResponseDto(OrdenEntity ordenEntity) {
        log.info("📦 [MAPEO] Iniciando mapeo Entity → DTO (numeroOrden={})", ordenEntity.getNumeroOrden());

        OrdenResponseDTO ordenResponseDTO = new OrdenResponseDTO();
        ordenResponseDTO.setNumeroOrden(ordenEntity.getNumeroOrden());
        ordenResponseDTO.setEstadoOrden(ordenEntity.getEstadoOrden());
        ordenResponseDTO.setNumeroFactura(ordenEntity.getNumeroFactura());
        ordenResponseDTO.setIdentificacionCliente(ordenEntity.getIdentificacionCliente());
        ordenResponseDTO.setNombreCliente(ordenEntity.getNombreCliente());
        ordenResponseDTO.setIdentificacionEmpleado(ordenEntity.getIdentificacionEmpleado());
        ordenResponseDTO.setNombreEmpleado(ordenEntity.getNombreEmpleado());
        ordenResponseDTO.setIdentificacionProveedor(ordenEntity.getIdentificacionProveedor());
        ordenResponseDTO.setNombreProveedor(ordenEntity.getNombreProveedor());
        ordenResponseDTO.setTotalCompra(ordenEntity.getTotalCompra());

        // 📦 Mapeo lista de detalles
        if (ordenEntity.getDetalles() != null && !ordenEntity.getDetalles().isEmpty()) {
            List<OrdenDetalleResponseDTO> detalles = new ArrayList<>();

            for (OrdenDetalleEntity detalle : ordenEntity.getDetalles()) {
                detalles.add(mapDetalleEntityToResponse(detalle));
            }

            ordenResponseDTO.setDetalles(detalles);
        }

        // 🕓 Formateo de fechas
        ordenFormatearFecha.asignarFechasFormateadas(ordenEntity, ordenResponseDTO);

        log.info("✅ [MAPEO] Mapeo completado Entity → DTO (numeroOrden={})", ordenEntity.getNumeroOrden());

        return ordenResponseDTO;
    }

    /**
     * 📦 Convierte un detalle de entidad en un DTO de respuesta.
     */
    public OrdenDetalleResponseDTO mapDetalleEntityToResponse(OrdenDetalleEntity detalleEntity) {
        log.debug("📦 [MAPEO] Mapeando Orden detalle Entity → DTO: código={}, producto={}", detalleEntity.getCodigo(), detalleEntity.getProducto());

        OrdenDetalleResponseDTO responseDTO = new OrdenDetalleResponseDTO();
        responseDTO.setCodigo(detalleEntity.getCodigo());
        responseDTO.setProducto(detalleEntity.getProducto());
        responseDTO.setDescripcion(detalleEntity.getDescripcion());
        responseDTO.setCantidad(detalleEntity.getCantidad());
        responseDTO.setPrecio(detalleEntity.getPrecio());

        if (detalleEntity.getFechaCreacion() != null) {
            responseDTO.setFechaCreacion(ordenFormatearFecha.formatearFecha(detalleEntity.getFechaCreacion()));
        }

        if (detalleEntity.getFechaActualizacion() != null) {
            responseDTO.setFechaActualizacion(ordenFormatearFecha.formatearFecha(detalleEntity.getFechaActualizacion()));
        }

        log.debug("✅ [MAPEO] Orden detalle mapeado correctamente: código={}, producto={}", detalleEntity.getCodigo(), detalleEntity.getProducto());

        return responseDTO;
    }

    /**
     * 🔄 Define el estado inicial de una orden.
     */
    public void mapEstadoOrden(OrdenEntity ordenEntity) {
        log.info("🔄 [SOLICITUD] Estableciendo estado inicial de orden: {}", ordenEntity.getNumeroOrden());

        ordenEntity.setFechaCreacion(LocalDateTime.now());
        ordenEntity.setEstadoOrden(ESTADO_ABIERTA);

        log.debug("✅ [FINALIZADO] Estado establecido a '{}'", ESTADO_ABIERTA);
    }

    /**
     * ✏️ Actualiza una orden existente con nuevos datos del request.
     */
    public void mapDetalleOrderExistente(OrdenEntity ordenEntity, OrdenRequestDTO requestDTO) {
        log.info("✏️ [SOLICITUD] Actualizando orden existente: {}", ordenEntity.getNumeroOrden());

        ordenEntity.setIdentificacionCliente(requestDTO.getIdentificacionCliente());
        ordenEntity.setNombreCliente(requestDTO.getNombreCliente());
        ordenEntity.setIdentificacionEmpleado(requestDTO.getIdentificacionEmpleado());
        ordenEntity.setNombreEmpleado(requestDTO.getNombreEmpleado());
        ordenEntity.setIdentificacionProveedor(requestDTO.getIdentificacionProveedor());
        ordenEntity.setNombreProveedor(requestDTO.getNombreProveedor());
        ordenEntity.setFechaActualizacion(LocalDateTime.now());

        log.info("✅ [FINALIZADO] Orden actualizada correctamente: {}", ordenEntity.getNumeroOrden());
    }
}
