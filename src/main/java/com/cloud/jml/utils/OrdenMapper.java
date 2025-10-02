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

    public OrdenEntity mapRequestDtoToEntity(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando mapeo DTO a Entity para crear Orden");

        OrdenEntity ordenEntity = new OrdenEntity();

        // generar numeroOrden (UUID)
        String generated = UUID.randomUUID().toString();
        ordenEntity.setNumeroOrden(generated);

        ordenEntity.setIdentificacionCliente(ordenRequestDTO.getIdentificacionCliente());
        ordenEntity.setNombreCliente(ordenRequestDTO.getNombreCliente());
        ordenEntity.setIdentificacionEmpleado(ordenRequestDTO.getIdentificacionEmpleado());
        ordenEntity.setNombreEmpleado(ordenRequestDTO.getNombreEmpleado());
        ordenEntity.setIdentificacionProveedor(ordenRequestDTO.getIdentificacionProveedor());
        ordenEntity.setNombreProveedor(ordenRequestDTO.getNombreProveedor());
        ordenEntity.setFechaCreacion(LocalDateTime.now());

        // 🔹 Mapear lista de detalles
        if (ordenRequestDTO.getDetalles() != null) {
            List<OrdenDetalleEntity> detalles = new ArrayList<>();

            for (OrdenDetalleRequestDTO detalleDTO : ordenRequestDTO.getDetalles()) {
                OrdenDetalleEntity detalleEntity = mapDetalleRequestToEntity(detalleDTO);
                detalleEntity.setFechaCreacion(LocalDateTime.now());
                detalleEntity.setOrden(ordenEntity);
                detalles.add(detalleEntity);
            }

            ordenEntity.setDetalles(detalles);
        }

        log.info("📌 Finalizando mapeo DTO a Entity para crear Orden (numeroOrden={})", generated);
        return ordenEntity;
    }

    public OrdenDetalleEntity mapDetalleRequestToEntity(OrdenDetalleRequestDTO detalleDTO) {
        log.info("📌 Mapeando detalle: codigo={}, producto={}", detalleDTO.getCodigo(), detalleDTO.getProducto());

        OrdenDetalleEntity detalleEntity = new OrdenDetalleEntity();

        detalleEntity.setCodigo(detalleDTO.getCodigo());
        detalleEntity.setProducto(detalleDTO.getProducto());
        detalleEntity.setDescripcion(detalleDTO.getDescripcion());
        detalleEntity.setCantidad(detalleDTO.getCantidad());
        detalleEntity.setPrecio(detalleDTO.getPrecio());

        log.info("📌 Detalle mapeado: codigo={}, producto={}", detalleDTO.getCodigo(), detalleDTO.getProducto());

        return detalleEntity;
    }

    public OrdenResponseDTO mapEntityToResponseDto(OrdenEntity ordenEntity) {
        log.info("📌 Iniciando mapeo Entity a DTO para devolver Orden");

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

        // 🔹 Mapear lista de detalles
        if (ordenEntity.getDetalles() != null) {
            List<OrdenDetalleResponseDTO> detalles = new ArrayList<>();
            for (OrdenDetalleEntity detalle : ordenEntity.getDetalles()) {
                detalles.add(mapDetalleEntityToResponse(detalle));
            }
            ordenResponseDTO.setDetalles(detalles);
        }

        // Fechas formateadas
        ordenFormatearFecha.asignarFechasFormateadas(ordenEntity, ordenResponseDTO);

        log.info("📌 Finalizando mapeo Entity a DTO para devolver Orden");

        return ordenResponseDTO;
    }

    public OrdenDetalleResponseDTO mapDetalleEntityToResponse(OrdenDetalleEntity detalleEntity) {
        log.info("📌 Mapeando Orden detalle: codigo={}, producto={}", detalleEntity.getCodigo(), detalleEntity.getProducto());

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

        log.info("📌 Orden detalle mapeado: codigo={}, producto={}", detalleEntity.getCodigo(), detalleEntity.getProducto());

        return responseDTO;
    }

    public void mapEstadoOrden(OrdenEntity ordenEntity) {
        log.info("📌 Actualizando estado de orden: {}", ordenEntity.getNumeroOrden());
        // fijar fecha + estado
        ordenEntity.setFechaCreacion(LocalDateTime.now());
        ordenEntity.setEstadoOrden(ESTADO_ABIERTA);

        log.info("📌 Finalizando actualización de estado de orden: {}", ordenEntity.getNumeroOrden());
    }

    public void mapDetalleOrderExistente(OrdenEntity ordenEntity, OrdenRequestDTO requestDTO) {
        log.info("📌 Actualizando detalles de orden existente: {}", ordenEntity.getNumeroOrden());

        ordenEntity.setIdentificacionCliente(requestDTO.getIdentificacionCliente());
        ordenEntity.setNombreCliente(requestDTO.getNombreCliente());
        ordenEntity.setIdentificacionEmpleado(requestDTO.getIdentificacionEmpleado());
        ordenEntity.setNombreEmpleado(requestDTO.getNombreEmpleado());
        ordenEntity.setIdentificacionProveedor(requestDTO.getIdentificacionProveedor());
        ordenEntity.setNombreProveedor(requestDTO.getNombreProveedor());
        ordenEntity.setFechaActualizacion(LocalDateTime.now());

        log.info("📌 Finalizando actualización de detalles de orden existente: {}", ordenEntity.getNumeroOrden());
    }
}
