package com.cloud.jml.utils;

import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.model.OrdenEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component // 🔹 Anotación para indicar que es un componente de Spring
public class OrdenMapper {

    private final OrdenUtils ordenUtils;

    public OrdenMapper(OrdenUtils ordenUtils) {
        this.ordenUtils = ordenUtils;
    }

    // ------------------ 🔹 Métodos privados de Mapeos ------------------

    public OrdenEntity mapRequestDtoToEntity(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando mapeo DTO a Entity para crear Producto");

        OrdenEntity ordenEntity = new OrdenEntity();

        ordenEntity.setCodigo(ordenRequestDTO.getCodigo());
        ordenEntity.setNombre(ordenRequestDTO.getNombre());
        ordenEntity.setDescripcion(ordenRequestDTO.getDescripcion());
        ordenEntity.setCantidad(ordenRequestDTO.getCantidad());
        ordenEntity.setPrecio(ordenRequestDTO.getPrecio());
        ordenEntity.setProveedorId(ordenRequestDTO.getProveedorId());
        ordenEntity.setProveedorName(ordenRequestDTO.getProveedorName());
        ordenEntity.setFechaCreacion(LocalDateTime.now());

        log.info("📌 Finalizando mapeo DTO a Entity para crear Producto");

        return ordenEntity;
    }

    public OrdenResponseDTO mapEntityToResponseDto(OrdenEntity ordenEntity) {
        log.info("📌 Iniciando mapeo Entity a DTO para crear Producto");

        OrdenResponseDTO ordenResponseDTO = new OrdenResponseDTO();

        ordenResponseDTO.setCodigo(ordenEntity.getCodigo());
        ordenResponseDTO.setNombre(ordenEntity.getNombre());
        ordenResponseDTO.setDescripcion(ordenEntity.getDescripcion());
        ordenResponseDTO.setCantidad(ordenEntity.getCantidad());
        ordenResponseDTO.setPrecio(ordenEntity.getPrecio());
        ordenResponseDTO.setProveedorId(ordenEntity.getProveedorId());
        ordenResponseDTO.setProveedorName(ordenEntity.getProveedorName());

        // 🔹 Formatear fechas
        ordenUtils.asignarFechasFormateadas(ordenEntity, ordenResponseDTO);

        log.info("📌 Finalizando mapeo Entity a DTO para crear Producto");

        return ordenResponseDTO;
    }
}
