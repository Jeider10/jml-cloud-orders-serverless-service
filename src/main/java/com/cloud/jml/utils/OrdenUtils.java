package com.cloud.jml.utils;

import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.exception.orders.OrdenPersistenceException;
import com.cloud.jml.exception.producto.ProductoNoEncontradoException;
import com.cloud.jml.model.OrdenDetalleEntity;
import com.cloud.jml.model.OrdenEntity;
import com.cloud.jml.repository.OrdenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component // 🔹 Anotación para indicar que es un componente de Spring
public class OrdenUtils {

    public static final String ESTADO_CERRADA = "CERRADA";

    private final OrdenRepository ordenRepository;
    private final OrdenMapper mapper;

    public OrdenUtils(OrdenRepository ordenRepository, OrdenMapper mapper) {
        this.ordenRepository = ordenRepository;
        this.mapper = mapper;
        log.info("🔥 OrdenUtils inicializado correctamente.");
    }

    public OrdenEntity guardarOrdenBD(OrdenEntity ordenEntity) {
        try {
            return ordenRepository.save(ordenEntity);

        } catch (DataIntegrityViolationException e) {
            log.error("🚨 Violación de integridad al guardar la orden: {}", e.getMessage(), e);
            throw new OrdenPersistenceException("Error de integridad en base de datos al guardar la orden", e);

        } catch (DataAccessException e) {
            log.error("🚨 Error de acceso a datos al guardar la orden: {}", e.getMessage(), e);
            throw new OrdenPersistenceException("Error al guardar la orden en la base de datos", e);

        } catch (Exception e) {
            log.error("🚨 Error inesperado al guardar la orden: {}", e.getMessage(), e);
            throw new OrdenPersistenceException("Error inesperado al registrar la orden", e);
        }
    }

    public void eliminarOrdenBD(OrdenEntity ordenEntity) {
        try {
            ordenRepository.delete(ordenEntity);

        } catch (DataIntegrityViolationException e) {
            log.error("🚨 Violación de integridad al eliminar la orden: {}", e.getMessage(), e);
            throw new OrdenPersistenceException("Error de integridad en base de datos al eliminar la orden", e);

        } catch (DataAccessException e) {
            log.error("🚨 Error de acceso a datos al eliminar la orden: {}", e.getMessage(), e);
            throw new OrdenPersistenceException("Error al eliminar la orden en la base de datos", e);

        } catch (Exception e) {
            log.error("🚨 Error inesperado al eliminar la orden: {}", e.getMessage(), e);
            throw new OrdenPersistenceException("Error inesperado al eliminar la orden", e);
        }
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
        asignarOrdenYFechaADetalles(ordenEntity);

        // 🔹 Calcular total inicial
        recalcularTotalCompra(ordenEntity);

        log.info("🆕 Nueva orden creada: {}", ordenEntity.getNumeroOrden());

        return ordenEntity;
    }

    public void asignarOrdenYFechaADetalles(OrdenEntity ordenEntity) {
        log.info("📌 Asignando orden y fecha a detalles para orden: {}", ordenEntity.getNumeroOrden());

        if (ordenEntity.getDetalles() != null) {
            for (OrdenDetalleEntity ordenDetalle : ordenEntity.getDetalles()) {
                ordenDetalle.setOrden(ordenEntity);
                ordenDetalle.setFechaCreacion(LocalDateTime.now());
            }
            log.info("📌 Detalles asignados a orden: {}", ordenEntity.getNumeroOrden());
        } else {
            log.warn("⚠️ No se encontraron detalles para la orden: {}", ordenEntity.getNumeroOrden());
        }
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

        long total = 0L;

        for (OrdenDetalleEntity ordenDetalle : ordenEntity.getDetalles()) {
            long cantidad = (ordenDetalle.getCantidad() != null) ? ordenDetalle.getCantidad() : 0L;
            long precio = (ordenDetalle.getPrecio() != null) ? ordenDetalle.getPrecio() : 0L;

            long subtotal = cantidad * precio;

            total += subtotal;
        }

        ordenEntity.setTotalCompra(total);

        log.info("📌 Total de compra recalculado: {}", total);
    }

    public OrdenDetalleEntity buscarDetallePorCodigo(OrdenEntity orden, Long codigoProducto) {
        log.info("🔍 Buscando detalle por código: {}", codigoProducto);

        for (OrdenDetalleEntity detalle : orden.getDetalles()) {
            if (codigoProducto != null && codigoProducto.equals(detalle.getCodigo())) {
                log.info("🔍 Detalle encontrado: {}", codigoProducto);
                return detalle;
            }
        }

        log.warn("⚠️ No se encontró detalle con código: {}", codigoProducto);
        throw new ProductoNoEncontradoException(codigoProducto);
    }

    public long obtenerCantidadActual(OrdenDetalleEntity detalle) {
        log.info("📌 Obteniendo cantidad actual del detalle: {}", detalle.getCodigo());

        if (detalle.getCantidad() != null) {
            log.info("📌 Cantidad actual: {}", detalle.getCantidad());
            return detalle.getCantidad();
        } else {
            log.info("📌 Cantidad actual: 0");
            return 0L;
        }
    }

    public void actualizarOEliminarDetalle(OrdenEntity orden, OrdenDetalleEntity detalle, Long codigoProducto, long cantidadARestar, long nuevaCantidad) {
        log.info("📌 Actualizando o eliminando detalle: producto(codigo)={}, cantidadARestar={}", codigoProducto, cantidadARestar);

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
        // 🔹 siempre actualizar la fecha de la orden
        orden.setFechaActualizacion(LocalDateTime.now());
    }

    public void mapCerrarOrden(OrdenEntity orden) {
        log.info("📌 Cerrando orden: {}", orden.getNumeroOrden());

        orden.setEstadoOrden(ESTADO_CERRADA);
        orden.setFechaActualizacion(LocalDateTime.now());

        log.info("📌 Orden cerrada: {}", orden.getNumeroOrden());
    }
}
