package com.cloud.jml.service;

import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.model.OrdenDetalleEntity;
import com.cloud.jml.model.OrdenEntity;
import com.cloud.jml.repository.OrdenRepository;
import com.cloud.jml.utils.OrdenMapper;
import com.cloud.jml.utils.OrdenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class OrdenService {

    public static final String ESTADO_ABIERTA = "ABIERTA";
    public static final String ESTADO_CERRADA = "CERRADA";

    private final OrdenRepository ordenRepository;
    private final OrdenMapper mapper;
    private final OrdenUtils ordenUtils;

    public OrdenService(OrdenRepository ordenRepository, OrdenMapper mapper, OrdenUtils ordenUtils) {
        this.ordenRepository = ordenRepository;
        this.mapper = mapper;
        this.ordenUtils = ordenUtils;
        log.info("🔥 OrdenService inicializado correctamente.");
    }

    @Transactional
    public OrdenResponseDTO crearOrdenDeVenta(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Creando/actualizando Orden de Venta para cliente: {}", ordenRequestDTO.getNombreCliente());

        Optional<OrdenEntity> existente = ordenRepository
                .findFirstByIdentificacionClienteAndEstadoOrden(ordenRequestDTO.getIdentificacionCliente(), ESTADO_ABIERTA);

        OrdenEntity ordenEntity;

        if (existente.isPresent()) {
            // Caso: Agregar detalles a orden existente
            ordenEntity = existente.get();
            log.warn("⚠️ Ya existe una Orden ABIERTA para el cliente {}. Se agregarán los nuevos detalles.", ordenRequestDTO.getIdentificacionCliente());

            ordenUtils.agregarDetallesOrdenExistente(ordenEntity, ordenRequestDTO);
        } else {
            // Caso: Crear nueva orden
            log.info("🆕 No existe orden ABIERTA para el cliente {}. Se creará una nueva.", ordenRequestDTO.getIdentificacionCliente());
            ordenEntity = ordenUtils.crearNuevaOrden(ordenRequestDTO);
        }

        OrdenEntity guardado = ordenRepository.save(ordenEntity);
        log.info("✅ Orden procesada correctamente. numeroOrden: {} con {} detalle(s)", guardado.getNumeroOrden(), guardado.getDetalles().size());

        return mapper.mapEntityToResponseDto(guardado);
    }

    @Transactional
    public OrdenResponseDTO restarCantidadProducto(String numeroOrden, Long codigoProducto, int cantidadARestar) {
        log.info("📌 Iniciando restarCantidadProducto -> numeroOrden: {}, codigoProducto: {}, cantidadARestar: {}", numeroOrden, codigoProducto, cantidadARestar);

        // 1) Buscar la orden ABIERTA
        Optional<OrdenEntity> ordenOpt = ordenRepository.findByNumeroOrdenAndEstadoOrden(numeroOrden, ESTADO_ABIERTA);
        if (ordenOpt.isEmpty()) {
            log.warn("⚠️ Orden no encontrada con numeroOrden: {}", numeroOrden);
            throw new RuntimeException("Orden no encontrada: " + numeroOrden);
        }

        OrdenEntity orden = ordenOpt.get();

        // 2) Buscar el detalle por codigo de producto
        OrdenDetalleEntity detalle = orden.getDetalles().stream()
                .filter(d -> d.getCodigo() != null && d.getCodigo().equals(codigoProducto))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en la orden: codigo " + codigoProducto));

        // 3) Validaciones básicas de cantidad
        if (cantidadARestar <= 0) {
            log.warn("⚠️ Cantidad inválida a restar: {}", cantidadARestar);
            throw new IllegalArgumentException("Cantidad a restar inválida");
        }

        Long cantidadActual = detalle.getCantidad() != null ? detalle.getCantidad() : 0L;
        if (cantidadActual < cantidadARestar) {
            log.warn("⚠️ Stock insuficiente en la orden. producto={}, cantidadActual={}, intentoRestar={}", codigoProducto, cantidadActual, cantidadARestar);
            throw new IllegalArgumentException("Stock insuficiente en la orden");
        }

        // 4) Calcular nueva cantidad
        long nuevaCantidad = cantidadActual - cantidadARestar;

        if (nuevaCantidad <= 0) {
            // eliminar el detalle de la orden
            orden.getDetalles().remove(detalle);
            log.info("🗑️ Detalle eliminado: producto(codigo)={} tras restar {}", codigoProducto, cantidadARestar);
        } else {
            // actualizar el detalle
            detalle.setCantidad(nuevaCantidad);
            detalle.setFechaActualizacion(LocalDateTime.now());
            log.info("✅ Cantidad actualizada en producto(codigo)={}, nuevaCantidad={}", codigoProducto, nuevaCantidad);
        }

        orden.setFechaActualizacion(LocalDateTime.now());

        // 🔹 Recalcular total después de restar/eliminar
        ordenUtils.recalcularTotalCompra(orden);

        OrdenEntity actualizado = ordenRepository.save(orden);

        return mapper.mapEntityToResponseDto(actualizado);
    }

    @Transactional
    public OrdenResponseDTO cerrarOrdenPorCliente(Long identificacionCliente) {
        log.info("📌 Cerrando orden del cliente: {}", identificacionCliente);

        Optional<OrdenEntity> ordenOpt = ordenRepository.findFirstByIdentificacionClienteAndEstadoOrden(identificacionCliente, ESTADO_ABIERTA);
        if (ordenOpt.isEmpty()) {
            throw new RuntimeException("No existe orden ABIERTA para este cliente");
        }

        OrdenEntity orden = ordenOpt.get();
        orden.setEstadoOrden(ESTADO_CERRADA);
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenEntity guardado = ordenRepository.save(orden);
        log.info("✅ Orden cerrada correctamente para cliente: {}", identificacionCliente);

        return mapper.mapEntityToResponseDto(guardado);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarOrdenesPorEstado(String estadoOrden) {
        List<OrdenEntity> ordenes = ordenRepository.findByEstadoOrden(estadoOrden);

        return ordenes.stream()
                .map(mapper::mapEntityToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarOrdenesPorClienteYEstado(Long identificacionCliente, String estadoOrden) {
        List<OrdenEntity> ordenes = ordenRepository.findByIdentificacionClienteAndEstadoOrden(identificacionCliente, estadoOrden);

        return ordenes.stream()
                .map(mapper::mapEntityToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarTodasLasOrdenes() {
        log.info("📌 Consultando todas las órdenes registradas en BD");

        List<OrdenEntity> ordenes = ordenRepository.findAll();

        log.info("📌 Total órdenes recuperadas: {}", ordenes.size());

        return ordenes.stream()
                .map(mapper::mapEntityToResponseDto)
                .toList();
    }
}
