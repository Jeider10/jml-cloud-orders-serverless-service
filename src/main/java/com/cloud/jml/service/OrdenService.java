package com.cloud.jml.service;

import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.exception.cantidad.CantidadInvalidaException;
import com.cloud.jml.exception.orders.OrdenNoEncontradaException;
import com.cloud.jml.exception.orders.OrdenPorClienteNoEncontradaException;
import com.cloud.jml.exception.stock.StockInsuficienteException;
import com.cloud.jml.model.OrdenDetalleEntity;
import com.cloud.jml.model.OrdenEntity;
import com.cloud.jml.repository.OrdenRepository;
import com.cloud.jml.utils.OrdenMapper;
import com.cloud.jml.utils.OrdenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class OrdenService {

    public static final String ESTADO_ABIERTA = "ABIERTA";

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

        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(guardado);
        log.info("📌 Orden procesada correctamente. numeroOrden: {}", ordenResponseDTO.getNumeroOrden());

        return ordenResponseDTO;
    }

    @Transactional
    public OrdenResponseDTO restarCantidadProducto(String numeroOrden, Long codigoProducto, int cantidadARestar) {
        log.info("📌 Iniciando restarCantidadProducto -> numeroOrden: {}, codigoProducto: {}, cantidadARestar: {}", numeroOrden, codigoProducto, cantidadARestar);

        // 🔹 1) Buscar la orden ABIERTA
        Optional<OrdenEntity> ordenOpt = ordenRepository.findByNumeroOrdenAndEstadoOrden(numeroOrden, ESTADO_ABIERTA);

        if (ordenOpt.isEmpty()) {
            log.warn("⚠️ Orden no encontrada con numeroOrden: {}", numeroOrden);
            throw new OrdenNoEncontradaException(numeroOrden);
        }

        OrdenEntity orden = ordenOpt.get();

        // 🔹 2) Buscar el detalle por código de producto
        OrdenDetalleEntity detalle = ordenUtils.buscarDetallePorCodigo(orden, codigoProducto);

        // 🔹 3) Validaciones básicas de cantidad
        if (cantidadARestar <= 0) {
            log.warn("⚠️ Cantidad inválida a restar: {}", cantidadARestar);
            throw new CantidadInvalidaException(cantidadARestar);
        }

        // 🔹 4) Obtener cantidad actual
        long cantidadActual = ordenUtils.obtenerCantidadActual(detalle);

        // 🔹 5) Validaciones básicas de cantidadActual < cantidadARestar
        if (cantidadActual < cantidadARestar) {
            log.warn("⚠️ Stock insuficiente en la orden. producto={}, cantidadActual={}, intentoRestar={}", codigoProducto, cantidadActual, cantidadARestar);
            throw new StockInsuficienteException(codigoProducto, cantidadActual, cantidadARestar);
        }

        // 🔹 6) Calcular nueva cantidad
        long nuevaCantidad = cantidadActual - cantidadARestar;

        // 🔹 7) Actualizar o eliminar detalle según corresponda
        ordenUtils.actualizarOEliminarDetalle(orden, detalle, codigoProducto, cantidadARestar, nuevaCantidad);

        // 🔹 8) Recalcular total después de restar/eliminar
        ordenUtils.recalcularTotalCompra(orden);

        // 🔹 Guardar en la base
        OrdenEntity actualizado = ordenRepository.save(orden);

        // 🔹 Mapeamos a ordenResponseDTO
        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(actualizado);
        log.info("✅ RestarCantidadProducto finalizado correctamente. numeroOrden: {}", ordenResponseDTO.getNumeroOrden());

        return ordenResponseDTO;
    }

    @Transactional
    public OrdenResponseDTO cerrarOrdenPorCliente(Long identificacionCliente) {
        log.info("📌 Cerrando orden del cliente: {}", identificacionCliente);

        Optional<OrdenEntity> ordenOpt = ordenRepository.findFirstByIdentificacionClienteAndEstadoOrden(identificacionCliente, ESTADO_ABIERTA);

        if (ordenOpt.isEmpty()) {
            log.warn("⚠️ No se encontró una orden ABIERTA para el cliente: {}", identificacionCliente);
            throw new OrdenPorClienteNoEncontradaException(identificacionCliente);
        }

        OrdenEntity orden = ordenOpt.get();
        ordenUtils.mapCerrarOrden(orden);

        OrdenEntity guardado = ordenRepository.save(orden);
        log.info("✅ Orden cerrada correctamente para cliente: {}", identificacionCliente);

        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(guardado);
        log.info("📌 Orden cerrada correctamente para cliente: {}", identificacionCliente);

        return ordenResponseDTO;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarOrdenesPorEstado(String estadoOrden) {
        log.info("📌 Consultando órdenes por estado: {}", estadoOrden);

        List<OrdenEntity> ordenes = ordenRepository.findByEstadoOrden(estadoOrden);

        // convertir a stream
        Stream<OrdenEntity> ordenesStream = ordenes.stream();

        // mapear entidades a DTOs
        Stream<OrdenResponseDTO> dtoStream = ordenesStream.map(mapper::mapEntityToResponseDto);

        // recolectar en lista
        List<OrdenResponseDTO> responseList = dtoStream.toList();
        log.info("📌 Total órdenes recuperadas: {} por estado: {}", responseList.size(), estadoOrden);

        return responseList;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarOrdenesPorClienteYEstado(Long identificacionCliente, String estadoOrden) {
        log.info("📌 Consultando órdenes por cliente: {} y estado: {}", identificacionCliente, estadoOrden);

        List<OrdenEntity> ordenes = ordenRepository.findByIdentificacionClienteAndEstadoOrden(identificacionCliente, estadoOrden);

        // convertir a stream
        Stream<OrdenEntity> ordenesStream = ordenes.stream();

        // mapear entidades a DTOs
        Stream<OrdenResponseDTO> dtoStream = ordenesStream.map(mapper::mapEntityToResponseDto);

        // recolectar en lista
        List<OrdenResponseDTO> responseList = dtoStream.toList();
        log.info("📌 Total órdenes recuperadas: {} por cliente: {} y estado: {}", responseList.size(), identificacionCliente, estadoOrden);

        return responseList;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarTodasLasOrdenes() {
        log.info("📌 Consultando todas las órdenes registradas en BD");

        List<OrdenEntity> ordenes = ordenRepository.findAll();

        log.info("📌 Total órdenes recuperadas: {}", ordenes.size());

        // convertir a stream
        Stream<OrdenEntity> ordenesStream = ordenes.stream();

        // mapear entidades a DTOs
        Stream<OrdenResponseDTO> dtoStream = ordenesStream.map(mapper::mapEntityToResponseDto);

        // recolectar en lista
        List<OrdenResponseDTO> responseList = dtoStream.toList();
        log.info("📌 Total órdenes recuperadas: {}", responseList.size());

        return responseList;
    }
}
