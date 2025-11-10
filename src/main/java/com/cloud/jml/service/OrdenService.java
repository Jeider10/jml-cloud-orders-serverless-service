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
import com.cloud.jml.utils.orders.OrdenMapper;
import com.cloud.jml.utils.orders.OrdenUtils;
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

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarTodasLasOrdenes() {
        log.info("🔍 [CONSULTA] Recuperando todas las órdenes desde la base de datos");

        List<OrdenEntity> ordenesEntity = ordenRepository.findAll();

        if (ordenesEntity.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron órdenes registradas en la base de datos");
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de órdenes a DTOs", ordenesEntity.size());

        // convertir a stream
        Stream<OrdenEntity> ordenesStream = ordenesEntity.stream();

        // mapear entidades a DTOs
        Stream<OrdenResponseDTO> dtoStream = ordenesStream.map(mapper::mapEntityToResponseDto);

        // recolectar en lista
        List<OrdenResponseDTO> ordenResponse = dtoStream.toList();

        log.info("✅ [FINALIZADO] Total de órdenes mapeadas y retornadas: {}", ordenResponse.size());

        return ordenResponse;
    }

    @Transactional
    public OrdenResponseDTO crearOrdenDeVenta(OrdenRequestDTO ordenRequestDTO) {
        log.info("🔍 [CONSULTA] Verificando si el cliente {} tiene una orden ABIERTA", ordenRequestDTO.getIdentificacionCliente());

        Optional<OrdenEntity> ordenExistente = ordenRepository
                .findFirstByIdentificacionClienteAndEstadoOrden(ordenRequestDTO.getIdentificacionCliente(), ESTADO_ABIERTA);

        OrdenEntity ordenEntity;

        if (ordenExistente.isPresent()) {
            // Caso: Agregar detalles a orden existente
            ordenEntity = ordenExistente.get();
            log.warn("⚠️ [RESULTADO] Ya existe una orden ABIERTA para el cliente {}. Se agregarán los nuevos detalles.", ordenRequestDTO.getIdentificacionCliente());
            ordenUtils.agregarDetallesOrdenExistente(ordenEntity, ordenRequestDTO);
        } else {
            log.info("🆕 [CREACIÓN] No se encontró orden ABIERTA para el cliente {}. Creando nueva orden.", ordenRequestDTO.getIdentificacionCliente());
            ordenEntity = ordenUtils.crearNuevaOrden(ordenRequestDTO);
        }

        OrdenEntity guardarOrden = ordenUtils.guardarOrdenBD(ordenEntity);
        log.info("💾 [PERSISTENCIA] Orden guardada exitosamente. numeroOrden: {} con {} detalle(s)", guardarOrden.getNumeroOrden(), guardarOrden.getDetalles().size());

        log.info("📦 [MAPEO] Transformando entidad de orden a DTO para respuesta");
        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(guardarOrden);
        log.info("📦 [MAPEO] Orden mapeada a DTO para respuesta. numeroOrden: {}", ordenResponseDTO.getNumeroOrden());

        log.info("✅ [FINALIZADO] Orden procesada correctamente. numeroOrden: {}", ordenResponseDTO.getNumeroOrden());

        return ordenResponseDTO;
    }

    @Transactional
    public OrdenResponseDTO restarCantidadProducto(String numeroOrden, Long codigoProducto, int cantidadARestar) {
        log.info("🔍 [CONSULTA] Verificando existencia de orden ABIERTA con numeroOrden: {}", numeroOrden);

        // 🔹 1) Buscar la orden ABIERTA
        Optional<OrdenEntity> ordenOpt = ordenRepository.findByNumeroOrdenAndEstadoOrden(numeroOrden, ESTADO_ABIERTA);

        if (ordenOpt.isEmpty()) {
            log.warn("❌ [ERROR] No se encontró una orden ABIERTA con numeroOrden: {}", numeroOrden);
            throw new OrdenNoEncontradaException(numeroOrden);
        }

        OrdenEntity orden = ordenOpt.get();

        // 🔹 2) Buscar el detalle por código de producto
        log.info("🔍 [CONSULTA] Buscando detalle del producto con código: {}", codigoProducto);
        OrdenDetalleEntity detalle = ordenUtils.buscarDetallePorCodigo(orden, codigoProducto);

        // 🔹 3) Validaciones básicas de cantidad
        if (cantidadARestar <= 0) {
            log.warn("⚠️ [VALIDACIÓN] Cantidad inválida a restar: {}", cantidadARestar);
            throw new CantidadInvalidaException(cantidadARestar);
        }

        // 🔹 4) Obtener cantidad actual
        long cantidadActual = ordenUtils.obtenerCantidadActual(detalle);
        log.info("📦 [DATOS] Cantidad actual del producto {}: {}", codigoProducto, cantidadActual);

        // 🔹 5) Validaciones básicas de cantidadActual < cantidadARestar
        if (cantidadActual < cantidadARestar) {
            log.warn("⚠️ [VALIDACIÓN] Stock insuficiente -> producto={}, cantidadActual={}, intentoRestar={}", codigoProducto, cantidadActual, cantidadARestar);
            throw new StockInsuficienteException(codigoProducto, cantidadActual, cantidadARestar);
        }

        // 🔹 6) Calcular nueva cantidad
        long nuevaCantidad = cantidadActual - cantidadARestar;
        log.info("📦 [CÁLCULO] Nueva cantidad resultante para producto {}: {}", codigoProducto, nuevaCantidad);

        // 🔹 7) Actualizar o eliminar detalle según corresponda
        log.info("🔧 [ACTUALIZACIÓN] Actualizando o eliminando detalle según cantidad resultante...");
        mapper.actualizarOEliminarDetalle(orden, detalle, codigoProducto, cantidadARestar, nuevaCantidad);

        // 🔹 8) Recalcular total después de restar/eliminar
        log.info("🔄 [RECALCULO] Recalculando total de la orden...");
        ordenUtils.recalcularTotalCompra(orden);

        // 🔹 Guardar en la base
        OrdenEntity actualizado = ordenUtils.guardarOrdenBD(orden);
        log.info("💾 [PERSISTENCIA] Orden actualizada y guardada en base de datos. numeroOrden: {}", actualizado.getNumeroOrden());

        // 🔹 Mapeamos a ordenResponseDTO
        log.info("📦 [MAPEO] Transformando entidad actualizada a DTO...");
        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(actualizado);

        log.info("✅ [FINALIZADO] Operación completada exitosamente. numeroOrden: {}", ordenResponseDTO.getNumeroOrden());

        return ordenResponseDTO;
    }

    @Transactional
    public OrdenResponseDTO cerrarOrdenPorCliente(Long identificacionCliente) {
        log.info("🔍 [CONSULTA] Buscando orden ABIERTA del cliente con identificación: {}", identificacionCliente);

        Optional<OrdenEntity> ordenOpt = ordenRepository.findFirstByIdentificacionClienteAndEstadoOrden(identificacionCliente, ESTADO_ABIERTA);

        if (ordenOpt.isEmpty()) {
            log.warn("❌ [ERROR] No se encontró una orden ABIERTA para el cliente: {}", identificacionCliente);
            throw new OrdenPorClienteNoEncontradaException(identificacionCliente);
        }

        OrdenEntity orden = ordenOpt.get();
        log.info("🔧 [ACTUALIZACIÓN] Cerrando orden del cliente con identificación: {}", identificacionCliente);
        ordenUtils.mapCerrarOrden(orden);

        OrdenEntity guardarOrden = ordenUtils.guardarOrdenBD(orden);
        log.info("💾 [PERSISTENCIA] Orden cerrada y guardada correctamente en la base de datos. numeroOrden: {}", guardarOrden.getNumeroOrden());

        log.info("📦 [MAPEO] Transformando entidad cerrada a DTO para respuesta");
        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(guardarOrden);

        log.info("✅ [FINALIZADO] Orden cerrada exitosamente para cliente con identificación: {}", identificacionCliente);

        return ordenResponseDTO;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarOrdenesPorEstado(String estadoOrden) {
        log.info("🔍 [CONSULTA] Recuperando órdenes con estado: {}", estadoOrden);

        List<OrdenEntity> ordenes = ordenRepository.findByEstadoOrden(estadoOrden);

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron órdenes con estado: {}", estadoOrden);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de órdenes a DTOs (estado: {})", ordenes.size(), estadoOrden);

        // convertir a stream
        Stream<OrdenEntity> ordenesStream = ordenes.stream();

        // mapear entidades a DTOs
        Stream<OrdenResponseDTO> dtoStream = ordenesStream.map(mapper::mapEntityToResponseDto);

        // recolectar en lista
        List<OrdenResponseDTO> responseList = dtoStream.toList();

        log.info("✅ [FINALIZADO] Total de órdenes mapeadas y retornadas: {} (estado: {})", responseList.size(), estadoOrden);

        return responseList;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarOrdenesPorClienteYEstado(Long identificacionCliente, String estadoOrden) {
        log.info("🔍 [CONSULTA] Recuperando órdenes del cliente: {} con estado: {}", identificacionCliente, estadoOrden);

        List<OrdenEntity> ordenes = ordenRepository.findByIdentificacionClienteAndEstadoOrden(identificacionCliente, estadoOrden);

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron órdenes del cliente: {} con estado: {}", identificacionCliente, estadoOrden);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de órdenes a DTOs (cliente: {}, estado: {})", ordenes.size(), identificacionCliente, estadoOrden);

        // convertir a stream
        Stream<OrdenEntity> ordenesStream = ordenes.stream();

        // mapear entidades a DTOs
        Stream<OrdenResponseDTO> dtoStream = ordenesStream.map(mapper::mapEntityToResponseDto);

        // recolectar en lista
        List<OrdenResponseDTO> responseList = dtoStream.toList();

        log.info("✅ [FINALIZADO] Total de órdenes mapeadas y retornadas: {} (cliente: {}, estado: {})", responseList.size(), identificacionCliente, estadoOrden);

        return responseList;
    }

    @Transactional
    public void eliminarOrdenCliente(String numeroOrden, Long identificacionCliente) {
        log.info("🔍 [CONSULTA] Buscando orden número: {} para cliente: {}", numeroOrden, identificacionCliente);

        Optional<OrdenEntity> ordenOptional = ordenRepository.findByNumeroOrdenAndIdentificacionCliente(numeroOrden, identificacionCliente);

        if (ordenOptional.isEmpty()) {
            log.warn("⚠️ [NO ENCONTRADA] No existe orden con número: {} para cliente: {}", numeroOrden, identificacionCliente);
            throw new OrdenPorClienteNoEncontradaException(identificacionCliente);
        }

        OrdenEntity ordenEntity = ordenOptional.get();
        log.info("📦 [ENCONTRADA] Orden localizada -> númeroOrden: {}, cliente: {}, estado: {}",
                ordenEntity.getNumeroOrden(), ordenEntity.getIdentificacionCliente(), ordenEntity.getEstadoOrden());

        ordenUtils.eliminarOrdenBD(ordenEntity);
        log.info("🗑️ [ELIMINADA] Orden eliminada exitosamente -> númeroOrden: {}, cliente: {}",
                ordenEntity.getNumeroOrden(), ordenEntity.getIdentificacionCliente());
    }
}
