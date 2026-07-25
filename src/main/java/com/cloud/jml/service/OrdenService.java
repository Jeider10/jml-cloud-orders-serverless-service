package com.cloud.jml.service;

import com.cloud.jml.dto.CerrarOrdenRequestDTO;
import com.cloud.jml.dto.OrdenPagoRequestDTO;
import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.exception.cantidad.CantidadInvalidaException;
import com.cloud.jml.exception.orders.OrdenNoEncontradaException;
import com.cloud.jml.exception.orders.OrdenPorClienteNoEncontradaException;
import com.cloud.jml.exception.stock.StockInsuficienteException;
import com.cloud.jml.model.OrdenDetalleEntity;
import com.cloud.jml.model.OrdenEntity;
import com.cloud.jml.model.OrdenPagoEntity;
import com.cloud.jml.repository.OrdenRepository;
import com.cloud.jml.utils.orders.OrdenMapper;
import com.cloud.jml.utils.orders.OrdenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        log.info("🔍 [CONSULTA] Recuperando todas las ordenes desde la base de datos (ordenadas por fecha)");

        List<OrdenEntity> ordenesEntity = ordenRepository.findAllByOrderByFechaCreacionAsc();

        if (ordenesEntity.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes registradas en la base de datos");
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de ordenes a DTOs", ordenesEntity.size());

        // convertir a stream
        Stream<OrdenEntity> ordenesStream = ordenesEntity.stream();

        // mapear entidades a DTOs
        Stream<OrdenResponseDTO> dtoStream = ordenesStream.map(mapper::mapEntityToResponseDto);

        // recolectar en lista
        List<OrdenResponseDTO> ordenResponse = dtoStream.toList();

        log.info("✅ [FINALIZADO] Total de ordenes mapeadas y retornadas: {}", ordenResponse.size());

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
            log.warn("⚠️ [RESULTADO] Ya existe una orden ABIERTA para el cliente {}. Se agregaran los nuevos detalles.", ordenRequestDTO.getIdentificacionCliente());
            ordenUtils.agregarDetallesOrdenExistente(ordenEntity, ordenRequestDTO);
        } else {
            log.info("🆕 [CREACION] No se encontro orden ABIERTA para el cliente {}. Creando nueva orden.", ordenRequestDTO.getIdentificacionCliente());
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

        // 1) Buscar la orden ABIERTA
        Optional<OrdenEntity> ordenOpt = ordenRepository.findByNumeroOrdenAndEstadoOrden(numeroOrden, ESTADO_ABIERTA);

        if (ordenOpt.isEmpty()) {
            log.warn("❌ [ERROR] No se encontro una orden ABIERTA con numeroOrden: {}", numeroOrden);
            throw new OrdenNoEncontradaException(numeroOrden);
        }

        OrdenEntity orden = ordenOpt.get();

        // 2) Buscar el detalle por codigo de producto
        log.info("🔍 [CONSULTA] Buscando detalle del producto con codigo: {}", codigoProducto);
        OrdenDetalleEntity detalle = ordenUtils.buscarDetallePorCodigo(orden, codigoProducto);

        // 3) Validaciones basicas de cantidad
        if (cantidadARestar <= 0) {
            log.warn("⚠️ [VALIDACION] Cantidad invalida a restar: {}", cantidadARestar);
            throw new CantidadInvalidaException(cantidadARestar);
        }

        // 4) Obtener cantidad actual
        long cantidadActual = ordenUtils.obtenerCantidadActual(detalle);
        log.info("📦 [DATOS] Cantidad actual del producto {}: {}", codigoProducto, cantidadActual);

        // 5) Validaciones basicas de cantidadActual < cantidadARestar
        if (cantidadActual < cantidadARestar) {
            log.warn("⚠️ [VALIDACION] Stock insuficiente -> producto={}, cantidadActual={}, intentoRestar={}", codigoProducto, cantidadActual, cantidadARestar);
            throw new StockInsuficienteException(codigoProducto, cantidadActual, cantidadARestar);
        }

        // 6) Calcular nueva cantidad
        long nuevaCantidad = cantidadActual - cantidadARestar;
        log.info("📦 [CALCULO] Nueva cantidad resultante para producto {}: {}", codigoProducto, nuevaCantidad);

        // 7) Actualizar o eliminar detalle segun corresponda
        log.info("🔧 [ACTUALIZACION] Actualizando o eliminando detalle segun cantidad resultante...");
        mapper.actualizarOEliminarDetalle(orden, detalle, codigoProducto, cantidadARestar, nuevaCantidad);

        // 8) Recalcular total despues de restar/eliminar
        log.info("🔄 [RECALCULO] Recalculando total de la orden...");
        ordenUtils.recalcularTotalCompra(orden);

        // Guardar en la base
        OrdenEntity actualizado = ordenUtils.guardarOrdenBD(orden);
        log.info("💾 [PERSISTENCIA] Orden actualizada y guardada en base de datos. numeroOrden: {}", actualizado.getNumeroOrden());

        // Mapeamos a ordenResponseDTO
        log.info("📦 [MAPEO] Transformando entidad actualizada a DTO...");
        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(actualizado);

        log.info("✅ [FINALIZADO] Operacion completada exitosamente. numeroOrden: {}", ordenResponseDTO.getNumeroOrden());

        return ordenResponseDTO;
    }

    @Transactional
    public OrdenResponseDTO cerrarOrdenPorCliente(Long identificacionCliente, Long valorRecibido) {
        log.info("🔍 [CONSULTA] Buscando orden ABIERTA del cliente con identificacion: {}", identificacionCliente);

        Optional<OrdenEntity> ordenOpt = ordenRepository.findFirstByIdentificacionClienteAndEstadoOrden(identificacionCliente, ESTADO_ABIERTA);

        if (ordenOpt.isEmpty()) {
            log.warn("❌ [ERROR] No se encontro una orden ABIERTA para el cliente: {}", identificacionCliente);
            throw new OrdenPorClienteNoEncontradaException(identificacionCliente);
        }

        OrdenEntity orden = ordenOpt.get();

        // Validar que la orden tenga productos antes de cerrar
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            log.warn("❌ [ERROR] La orden no tiene productos. No se puede cerrar.");
            throw new OrdenNoEncontradaException(orden.getNumeroOrden());
        }

        log.info("🔧 [ACTUALIZACION] Cerrando orden del cliente con identificacion: {}", identificacionCliente);
        ordenUtils.mapCerrarOrden(orden);

        // Guardar valor recibido del cliente
        orden.setValorRecibido(valorRecibido);

        // Generar y asignar CUFE a la orden
        ordenUtils.generarAsignarCUFE(orden);

        OrdenEntity guardarOrden = ordenUtils.guardarOrdenBD(orden);
        log.info("💾 [PERSISTENCIA] Orden cerrada y guardada correctamente en la base de datos. numeroOrden: {}", guardarOrden.getNumeroOrden());

        log.info("📦 [MAPEO] Transformando entidad cerrada a DTO para respuesta");
        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(guardarOrden);

        log.info("✅ [FINALIZADO] Orden cerrada exitosamente para cliente con identificacion: {}", identificacionCliente);

        return ordenResponseDTO;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarOrdenesPorEstado(String estadoOrden) {
        log.info("🔍 [CONSULTA] Recuperando ordenes con estado: {}", estadoOrden);

        List<OrdenEntity> ordenes = ordenRepository.findByEstadoOrdenOrderByFechaCreacionAsc(estadoOrden);

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes con estado: {}", estadoOrden);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de ordenes a DTOs (estado: {})", ordenes.size(), estadoOrden);

        // convertir a stream
        Stream<OrdenEntity> ordenesStream = ordenes.stream();

        // mapear entidades a DTOs
        Stream<OrdenResponseDTO> dtoStream = ordenesStream.map(mapper::mapEntityToResponseDto);

        // recolectar en lista
        List<OrdenResponseDTO> responseList = dtoStream.toList();

        log.info("✅ [FINALIZADO] Total de ordenes mapeadas y retornadas: {} (estado: {})", responseList.size(), estadoOrden);

        return responseList;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarOrdenesPorClienteYEstado(Long identificacionCliente, String estadoOrden) {
        log.info("🔍 [CONSULTA] Recuperando ordenes del cliente: {} con estado: {}", identificacionCliente, estadoOrden);

        List<OrdenEntity> ordenes = ordenRepository.findByIdentificacionClienteAndEstadoOrden(identificacionCliente, estadoOrden);

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes del cliente: {} con estado: {}", identificacionCliente, estadoOrden);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de ordenes a DTOs (cliente: {}, estado: {})", ordenes.size(), identificacionCliente, estadoOrden);

        // convertir a stream
        Stream<OrdenEntity> ordenesStream = ordenes.stream();

        // mapear entidades a DTOs
        Stream<OrdenResponseDTO> dtoStream = ordenesStream.map(mapper::mapEntityToResponseDto);

        // recolectar en lista
        List<OrdenResponseDTO> responseList = dtoStream.toList();

        log.info("✅ [FINALIZADO] Total de ordenes mapeadas y retornadas: {} (cliente: {}, estado: {})", responseList.size(), identificacionCliente, estadoOrden);

        return responseList;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> obtenerOrdenesPorFechaCreacion(String fechaInicio, String fechaFin) {
        log.info("🔍 [CONSULTA] Iniciando busqueda de ordenes por rango de fecha de creacion: {} - {}", fechaInicio, fechaFin);

        LocalDateTime inicio = ordenUtils.parsearFechaInicio(fechaInicio);
        LocalDateTime fin = ordenUtils.parsearFechaFin(fechaFin);

        log.info("📅 [RANGO] Buscando ordenes entre {} y {}", inicio, fin);

        List<OrdenEntity> ordenEntity = ordenRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(inicio, fin);

        if (ordenEntity.isEmpty()) {
            log.warn("❌ [RESULTADO] No se encontraron ordenes en el rango de fechas: {} - {}", inicio, fin);
            return List.of();
        }

        List<OrdenResponseDTO> ordenResponse = ordenEntity.stream()
                .map(mapper::mapEntityToResponseDto)
                .toList();

        log.info("✅ [FINALIZADO] Ordenes encontradas en rango de fechas. Total: {}", ordenResponse.size());

        return ordenResponse;
    }

    @Transactional
    public void eliminarOrdenCliente(String numeroOrden, Long identificacionCliente) {
        log.info("🔍 [CONSULTA] Buscando orden numero: {} para cliente: {}", numeroOrden, identificacionCliente);

        Optional<OrdenEntity> ordenOptional = ordenRepository.findByNumeroOrdenAndIdentificacionCliente(numeroOrden, identificacionCliente);

        if (ordenOptional.isEmpty()) {
            log.warn("⚠️ [NO ENCONTRADA] No existe orden con numero: {} para cliente: {}", numeroOrden, identificacionCliente);
            throw new OrdenPorClienteNoEncontradaException(identificacionCliente);
        }

        OrdenEntity ordenEntity = ordenOptional.get();
        log.info("📦 [ENCONTRADA] Orden localizada -> numeroOrden: {}, cliente: {}, estado: {}",
                ordenEntity.getNumeroOrden(), ordenEntity.getIdentificacionCliente(), ordenEntity.getEstadoOrden());

        // Validar que la orden este ABIERTA antes de eliminar
        if (!ESTADO_ABIERTA.equals(ordenEntity.getEstadoOrden())) {
            log.warn("❌ [ERROR] No se puede eliminar una orden con estado: {}", ordenEntity.getEstadoOrden());
            throw new OrdenNoEncontradaException(numeroOrden);
        }

        ordenUtils.eliminarOrdenBD(ordenEntity);
        log.info("🗑️ [ELIMINADA] Orden eliminada exitosamente -> numeroOrden: {}, cliente: {}",
                ordenEntity.getNumeroOrden(), ordenEntity.getIdentificacionCliente());
    }

    @Transactional
    public OrdenResponseDTO cerrarOrdenConPagosMixtos(CerrarOrdenRequestDTO cerrarRequest) {
        Long identificacionCliente = cerrarRequest.getIdentificacionCliente();
        log.info("🔍 [CONSULTA] Buscando orden para cierre con pagos mixtos. Cliente: {}, NumeroOrden: {}",
                identificacionCliente, cerrarRequest.getNumeroOrden());

        Optional<OrdenEntity> ordenOpt;

        // Si viene numeroOrden especifico, buscar directamente por el
        if (cerrarRequest.getNumeroOrden() != null && !cerrarRequest.getNumeroOrden().isBlank()) {
            ordenOpt = ordenRepository.findById(cerrarRequest.getNumeroOrden());
        } else {
            // Buscar primero ABIERTA, si no existe buscar PENDIENTE
            ordenOpt = ordenRepository.findFirstByIdentificacionClienteAndEstadoOrden(identificacionCliente, ESTADO_ABIERTA);
            if (ordenOpt.isEmpty()) {
                ordenOpt = ordenRepository.findFirstByIdentificacionClienteAndEstadoOrden(identificacionCliente, "PENDIENTE");
            }
        }

        if (ordenOpt.isEmpty()) {
            log.warn("❌ [ERROR] No se encontro orden para el cliente: {}", identificacionCliente);
            throw new OrdenPorClienteNoEncontradaException(identificacionCliente);
        }

        OrdenEntity orden = ordenOpt.get();

        // Validar que la orden tenga productos antes de cerrar
        if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
            log.warn("❌ [ERROR] La orden no tiene productos. No se puede cerrar.");
            throw new OrdenNoEncontradaException(orden.getNumeroOrden());
        }

        // 🔹 Persistir datos de descuento
        if (cerrarRequest.getDescuentoTipo() != null && !cerrarRequest.getDescuentoTipo().isBlank()) {
            orden.setDescuentoTipo(cerrarRequest.getDescuentoTipo());
            orden.setDescuentoValor(cerrarRequest.getDescuentoValor());
            orden.setDescuentoAplicado(cerrarRequest.getDescuentoAplicado());
            log.info("🏷️ [DESCUENTO] Tipo: {}, Valor: {}, Aplicado: {}",
                    cerrarRequest.getDescuentoTipo(), cerrarRequest.getDescuentoValor(), cerrarRequest.getDescuentoAplicado());
        }

        // 🔹 Persistir comentario/observacion
        if (cerrarRequest.getComentario() != null && !cerrarRequest.getComentario().isBlank()) {
            orden.setComentario(cerrarRequest.getComentario());
            log.info("📝 [COMENTARIO] {}", cerrarRequest.getComentario());
        }

        // 🔹 Registrar pagos mixtos
        if (cerrarRequest.getPagos() != null && !cerrarRequest.getPagos().isEmpty()) {

            for (OrdenPagoRequestDTO pagoDTO : cerrarRequest.getPagos()) {

                if (pagoDTO.getValor() != null && pagoDTO.getValor() > 0) {

                    OrdenPagoEntity pagoEntity = new OrdenPagoEntity();
                    pagoEntity.setMetodoPago(pagoDTO.getMetodoPago());
                    pagoEntity.setValor(pagoDTO.getValor());
                    pagoEntity.setReferencia(pagoDTO.getReferencia());
                    pagoEntity.setFechaCreacion(java.time.LocalDateTime.now());
                    pagoEntity.setOrden(orden);

                    orden.getPagos().add(pagoEntity);

                    log.info("💳 [PAGO] Registrado: metodo={}, valor={}, referencia={}",
                            pagoDTO.getMetodoPago(), pagoDTO.getValor(), pagoDTO.getReferencia());
                }
            }
        }

        // Guardar valor recibido total
        orden.setValorRecibido(cerrarRequest.getValorRecibido());

        // 🔹 Determinar estado: PENDIENTE si pago insuficiente, CERRADA si pago completo
        Long totalCompra = orden.getTotalCompra() != null ? orden.getTotalCompra() : 0L;
        Long descuentoApl = orden.getDescuentoAplicado() != null ? orden.getDescuentoAplicado() : 0L;
        Long totalAPagar = totalCompra - descuentoApl;
        Long valorRecibido = cerrarRequest.getValorRecibido() != null ? cerrarRequest.getValorRecibido() : 0L;

        if (valorRecibido < totalAPagar) {
            // Pago insuficiente → estado PENDIENTE
            orden.setEstadoOrden("PENDIENTE");
            orden.setFechaActualizacion(java.time.LocalDateTime.now());
            log.info("⏳ [ESTADO] Orden marcada como PENDIENTE (pago insuficiente). Recibido: {}, Total: {}", valorRecibido, totalAPagar);
        } else {
            // Pago completo → estado CERRADA
            ordenUtils.mapCerrarOrden(orden);
            // Generar y asignar CUFE solo para ventas cerradas (pago completo)
            ordenUtils.generarAsignarCUFE(orden);

            // 🔹 Auto-generar comentario de cierre si la orden venia de PENDIENTE
            String comentarioAnterior = orden.getComentario();
            if (comentarioAnterior != null && !comentarioAnterior.isBlank()) {
                // Si el usuario no envio un nuevo comentario, agregar info de pago final
                if (cerrarRequest.getComentario() == null || cerrarRequest.getComentario().isBlank()) {
                    String comentarioFinal = comentarioAnterior + " | Pago final: $" + valorRecibido + " - Deuda saldada";
                    orden.setComentario(comentarioFinal);
                    log.info("📝 [COMENTARIO AUTO] {}", comentarioFinal);
                }
            }
        }

        OrdenEntity guardarOrden = ordenUtils.guardarOrdenBD(orden);
        log.info("💾 [PERSISTENCIA] Orden cerrada con pagos mixtos. numeroOrden: {}", guardarOrden.getNumeroOrden());

        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(guardarOrden);

        log.info("✅ [FINALIZADO] Orden cerrada exitosamente con {} pago(s) para cliente: {}",
                orden.getPagos().size(), identificacionCliente);

        return ordenResponseDTO;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> buscarPorNombreCliente(String nombreCliente) {
        log.info("🔍 [CONSULTA] Buscar ordenes por nombre cliente: {}", nombreCliente);

        List<OrdenEntity> ordenes = ordenRepository.findByNombreClienteContainingIgnoreCaseOrderByFechaCreacionAsc(nombreCliente);

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes para el cliente: {}", nombreCliente);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de ordenes a DTOs (cliente: {})", ordenes.size(), nombreCliente);

        List<OrdenResponseDTO> result = ordenes.stream().map(mapper::mapEntityToResponseDto).toList();

        log.info("✅ [FINALIZADO] Total de ordenes encontradas por cliente '{}': {}", nombreCliente, result.size());

        return result;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> buscarPorIdCliente(Long idCliente) {
        log.info("🔍 [CONSULTA] Buscar ordenes por ID cliente: {}", idCliente);

        List<OrdenEntity> ordenes = ordenRepository.findByIdentificacionClienteOrderByFechaCreacionAsc(idCliente);

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes para el ID cliente: {}", idCliente);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de ordenes a DTOs (ID cliente: {})", ordenes.size(), idCliente);

        List<OrdenResponseDTO> result = ordenes.stream().map(mapper::mapEntityToResponseDto).toList();

        log.info("✅ [FINALIZADO] Total de ordenes encontradas por ID cliente {}: {}", idCliente, result.size());

        return result;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> buscarPorNombreVendedor(String nombreVendedor) {
        log.info("🔍 [CONSULTA] Buscar ordenes por nombre vendedor: {}", nombreVendedor);

        List<OrdenEntity> ordenes = ordenRepository.findByNombreEmpleadoContainingIgnoreCaseOrderByFechaCreacionAsc(nombreVendedor);

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes para el vendedor: {}", nombreVendedor);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de ordenes a DTOs (vendedor: {})", ordenes.size(), nombreVendedor);

        List<OrdenResponseDTO> result = ordenes.stream().map(mapper::mapEntityToResponseDto).toList();

        log.info("✅ [FINALIZADO] Total de ordenes encontradas por vendedor '{}': {}", nombreVendedor, result.size());

        return result;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> buscarPorIdVendedor(Long idVendedor) {
        log.info("🔍 [CONSULTA] Buscar ordenes por ID vendedor: {}", idVendedor);

        List<OrdenEntity> ordenes = ordenRepository.findByIdentificacionEmpleadoOrderByFechaCreacionAsc(idVendedor);

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes para el ID vendedor: {}", idVendedor);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de ordenes a DTOs (ID vendedor: {})", ordenes.size(), idVendedor);

        List<OrdenResponseDTO> result = ordenes.stream().map(mapper::mapEntityToResponseDto).toList();

        log.info("✅ [FINALIZADO] Total de ordenes encontradas por ID vendedor {}: {}", idVendedor, result.size());

        return result;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> buscarPorNumeroFactura(String factura) {
        log.info("🔍 [CONSULTA] Buscar ordenes por numero factura: {}", factura);

        List<OrdenEntity> ordenes = ordenRepository.findByNumeroFacturaContainingIgnoreCaseOrderByFechaCreacionAsc(factura);

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes con numero de factura: {}", factura);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de ordenes a DTOs (factura: {})", ordenes.size(), factura);

        List<OrdenResponseDTO> result = ordenes.stream().map(mapper::mapEntityToResponseDto).toList();

        log.info("✅ [FINALIZADO] Total de ordenes encontradas por factura '{}': {}", factura, result.size());

        return result;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> buscarPorProducto(String producto) {
        log.info("🔍 [CONSULTA] Buscar ordenes por producto: {}", producto);

        List<OrdenEntity> todas = ordenRepository.findAllByOrderByFechaCreacionAsc();

        log.info("📦 [DATOS] Total de ordenes recuperadas para filtrar por producto: {}", todas.size());

        String prodLower = producto.toLowerCase();
        List<OrdenEntity> filtradas = todas.stream()
                .filter(o -> o.getDetalles() != null && o.getDetalles().stream()
                        .anyMatch(d -> d.getProducto() != null && d.getProducto().toLowerCase().contains(prodLower)))
                .toList();

        if (filtradas.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes con el producto: {}", producto);
            return List.of();
        }

        log.info("📦 [MAPEO] Transformando {} entidades de ordenes a DTOs (producto: {})", filtradas.size(), producto);

        List<OrdenResponseDTO> result = filtradas.stream().map(mapper::mapEntityToResponseDto).toList();

        log.info("✅ [FINALIZADO] Total de ordenes encontradas con producto '{}': {}", producto, result.size());

        return result;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> buscarOrdenes(String estado, String cliente, Long idCliente,
                                                String vendedor, Long idVendedor, String factura,
                                                String producto, String fechaInicio, String fechaFin) {
        log.info("🔍 [CONSULTA] Busqueda con filtros multiples");

        List<OrdenEntity> ordenes;

        // Priorizar filtros más específicos
        if (fechaInicio != null && !fechaInicio.isBlank() && fechaFin != null && !fechaFin.isBlank()) {
            LocalDateTime inicio = ordenUtils.parsearFechaInicio(fechaInicio);
            LocalDateTime fin = ordenUtils.parsearFechaFin(fechaFin);
            ordenes = ordenRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(inicio, fin);
        } else if (idVendedor != null) {
            ordenes = ordenRepository.findByIdentificacionEmpleadoOrderByFechaCreacionAsc(idVendedor);
        } else if (vendedor != null && !vendedor.isBlank()) {
            ordenes = ordenRepository.findByNombreEmpleadoContainingIgnoreCaseOrderByFechaCreacionAsc(vendedor);
        } else if (idCliente != null) {
            ordenes = ordenRepository.findByIdentificacionClienteOrderByFechaCreacionAsc(idCliente);
        } else if (cliente != null && !cliente.isBlank()) {
            ordenes = ordenRepository.findByNombreClienteContainingIgnoreCaseOrderByFechaCreacionAsc(cliente);
        } else if (factura != null && !factura.isBlank()) {
            ordenes = ordenRepository.findByNumeroFacturaContainingIgnoreCaseOrderByFechaCreacionAsc(factura);
        } else if (estado != null && !estado.isBlank()) {
            ordenes = ordenRepository.findByEstadoOrdenOrderByFechaCreacionAsc(estado);
        } else {
            ordenes = ordenRepository.findAllByOrderByFechaCreacionAsc();
        }

        if (ordenes.isEmpty()) {
            log.warn("⚠️ [RESULTADO] No se encontraron ordenes con los filtros proporcionados.");
            return List.of();
        }

        // Filtros adicionales en memoria (si se combinan)
        Stream<OrdenEntity> stream = ordenes.stream();

        if (estado != null && !estado.isBlank() && (fechaInicio != null || idVendedor != null || vendedor != null || idCliente != null || cliente != null || factura != null)) {
            stream = stream.filter(o -> estado.equalsIgnoreCase(o.getEstadoOrden()));
        }

        if (producto != null && !producto.isBlank()) {
            String prodLower = producto.toLowerCase();
            stream = stream.filter(o -> o.getDetalles() != null && o.getDetalles().stream()
                    .anyMatch(d -> d.getProducto() != null && d.getProducto().toLowerCase().contains(prodLower)));
        }

        List<OrdenResponseDTO> result = stream.map(mapper::mapEntityToResponseDto).toList();

        log.info("✅ [FINALIZADO] Busqueda completada. Resultados: {}", result.size());

        return result;
    }
}
