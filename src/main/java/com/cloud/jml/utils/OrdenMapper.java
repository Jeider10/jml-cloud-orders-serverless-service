package com.cloud.jml.utils;

import com.cloud.jml.dto.*;
import com.cloud.jml.model.OrdenDetalleEntity;
import com.cloud.jml.model.OrdenEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class OrdenMapper {

    private final OrdenUtils ordenUtils;

    public OrdenMapper(OrdenUtils ordenUtils) {
        this.ordenUtils = ordenUtils;
    }

    // ------------------ 🔹 DTO → Entity ------------------
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

        if (ordenRequestDTO.getDetalles() != null) {
            List<OrdenDetalleEntity> detalles = ordenRequestDTO.getDetalles().stream()
                    .map(this::mapDetalleRequestToEntity)
                    .map(d -> {
                        d.setFechaCreacion(LocalDateTime.now());
                        return d;
                    })
                    .toList(); // nueva lista independiente para cada orden
            detalles.forEach(d -> d.setOrden(ordenEntity));
            ordenEntity.setDetalles(detalles);
        }

        log.info("📌 Finalizando mapeo DTO a Entity para crear Orden (numeroOrden={})", generated);
        return ordenEntity;
    }

    public OrdenDetalleEntity mapDetalleRequestToEntity(OrdenDetalleRequestDTO detalleDTO) {
        OrdenDetalleEntity detalleEntity = new OrdenDetalleEntity();
        detalleEntity.setCodigo(detalleDTO.getCodigo());
        detalleEntity.setProducto(detalleDTO.getProducto());
        detalleEntity.setDescripcion(detalleDTO.getDescripcion());
        detalleEntity.setCantidad(detalleDTO.getCantidad());
        detalleEntity.setPrecio(detalleDTO.getPrecio());
        return detalleEntity;
    }

    // ------------------ 🔹 Entity → DTO ------------------
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

        // 🔹 Mapear lista de detalles
        if (ordenEntity.getDetalles() != null) {
            List<OrdenDetalleResponseDTO> detalles = ordenEntity.getDetalles().stream()
                    .map(this::mapDetalleEntityToResponse)
                    .toList();
            ordenResponseDTO.setDetalles(detalles);
        }

        // Fechas formateadas
        ordenUtils.asignarFechasFormateadas(ordenEntity, ordenResponseDTO);

        log.info("📌 Finalizando mapeo Entity a DTO para devolver Orden");
        return ordenResponseDTO;
    }

    public OrdenDetalleResponseDTO mapDetalleEntityToResponse(OrdenDetalleEntity detalleEntity) {
        OrdenDetalleResponseDTO responseDTO = new OrdenDetalleResponseDTO();
        responseDTO.setCodigo(detalleEntity.getCodigo());
        responseDTO.setProducto(detalleEntity.getProducto());
        responseDTO.setDescripcion(detalleEntity.getDescripcion());
        responseDTO.setCantidad(detalleEntity.getCantidad());
        responseDTO.setPrecio(detalleEntity.getPrecio());

        if (detalleEntity.getFechaCreacion() != null) {
            responseDTO.setFechaCreacion(ordenUtils.formatearFecha(detalleEntity.getFechaCreacion()));
        }
        if (detalleEntity.getFechaActualizacion() != null) {
            responseDTO.setFechaActualizacion(ordenUtils.formatearFecha(detalleEntity.getFechaActualizacion()));
        }

        return responseDTO;
    }
}
