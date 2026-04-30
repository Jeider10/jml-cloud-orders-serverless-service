package com.cloud.jml.utils.orders;

import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.exception.orders.OrdenDeletionException;
import com.cloud.jml.exception.orders.OrdenPersistenceException;
import com.cloud.jml.exception.producto.ProductoNoEncontradoException;
import com.cloud.jml.model.OrdenDetalleEntity;
import com.cloud.jml.model.OrdenEntity;
import com.cloud.jml.repository.OrdenRepository;
import com.cloud.jml.utils.dian.CUFEGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component // Anotacion para indicar que es un componente de Spring
public class OrdenUtils {

    public static final String ESTADO_CERRADA = "CERRADA";

    private final OrdenRepository ordenRepository;
    private final OrdenMapper mapper;

    public OrdenUtils(OrdenRepository ordenRepository, OrdenMapper mapper) {
        this.ordenRepository = ordenRepository;
        this.mapper = mapper;
        log.info("🔥 OrdenUtils inicializado correctamente.");
    }

    /**
     * Guarda la orden en BD con manejo de excepciones.
     */
    public OrdenEntity guardarOrdenBD(OrdenEntity ordenEntity) {
        try {
            return ordenRepository.save(ordenEntity);

        } catch (DataIntegrityViolationException e) {
            log.error("🚨 Violacion de integridad al guardar la orden: {}", e.getMessage(), e);
            throw OrdenPersistenceException.integrityViolation(e);

        } catch (DataAccessException e) {
            log.error("🚨 Error de acceso a datos al guardar la orden: {}", e.getMessage(), e);
            throw OrdenPersistenceException.dataAccessError(e);

        } catch (Exception e) {
            log.error("🚨 Error inesperado al guardar la orden: {}", e.getMessage(), e);
            throw OrdenPersistenceException.unexpected(e);
        }
    }

    /**
     * Elimina la orden de BD con manejo de excepciones.
     */
    public void eliminarOrdenBD(OrdenEntity ordenEntity) {
        try {
            ordenRepository.delete(ordenEntity);

        } catch (DataIntegrityViolationException e) {
            log.error("🚨 Violacion de integridad al eliminar la orden: {}", e.getMessage(), e);
            throw OrdenDeletionException.integrityViolation(e);

        } catch (DataAccessException e) {
            log.error("🚨 Error de acceso a datos al eliminar la orden: {}", e.getMessage(), e);
            throw OrdenDeletionException.dataAccessError(e);

        } catch (Exception e) {
            log.error("🚨 Error inesperado al eliminar la orden: {}", e.getMessage(), e);
            throw OrdenDeletionException.unexpected(e);
        }
    }

    /**
     * Crea una nueva orden desde un DTO de solicitud.
     */
    public OrdenEntity crearNuevaOrden(OrdenRequestDTO requestDTO) {
        log.info("🆕 [SOLICITUD] Creando nueva orden para cliente: {}", requestDTO.getIdentificacionCliente());

        OrdenEntity ordenEntity = mapper.mapRequestDtoToEntity(requestDTO);

        // Generar UUID si el mapper no lo asigno
        if (ordenEntity.getNumeroFactura() == null || ordenEntity.getNumeroFactura().isBlank()) {

            String numeroFactura = generarNumeroFactura(ordenEntity.getNumeroOrden());
            ordenEntity.setNumeroFactura(numeroFactura);

            log.info("🧮 [SOLICITUD] Numero de factura generado: {}", numeroFactura);
        }

        mapper.mapEstadoOrden(ordenEntity);

        // Asignar fechas de creacion en detalles y enlace orden->detalle ya hecho en mapper
        asignarOrdenYFechaADetalles(ordenEntity);

        // Calcular total inicial
        recalcularTotalCompra(ordenEntity);

        log.info("🆕 [FINALIZADO] Nueva orden creada: {}", ordenEntity.getNumeroOrden());

        return ordenEntity;
    }

    public String generarNumeroFactura(String numeroOrden) {

        // Encontrar el indice del primer guion (-)
        // Si no encuentra el '-', indexOf devuelve -1, lo cual causaria un error en substring.
        // Aunque se asume formato UUID, se agrega una pequena validacion basica para evitar errores.
        int primerGuion = numeroOrden.indexOf('-');
        int segundoGuion = numeroOrden.indexOf('-', primerGuion + 1);

        // Asegurarse de que el guion exista antes de intentar el substring
        if (primerGuion == -1 || segundoGuion == -1) {
            throw new IllegalArgumentException("El numero de orden no contiene un guion y no es un UUID valido.");
        }

        // Extraer la subcadena antes del primer guion
        String parteUUID = numeroOrden.substring(0, segundoGuion);
        log.info("📦 Numero de factura: {}", parteUUID);

        // Concatenar con la identificacion del cliente y devolver
//        return ordenEntity.getIdentificacionCliente() + "-" + parteUUID;
        return parteUUID;
    }

    /**
     * Asigna la orden y fecha de creacion a cada detalle.
     */
    public void asignarOrdenYFechaADetalles(OrdenEntity ordenEntity) {
        log.info("🗂️ [SOLICITUD] Asignando orden y fecha a detalles (numeroOrden={})", ordenEntity.getNumeroOrden());

        if (ordenEntity.getDetalles() != null && !ordenEntity.getDetalles().isEmpty()) {
            for (OrdenDetalleEntity ordenDetalle : ordenEntity.getDetalles()) {
                ordenDetalle.setOrden(ordenEntity);
                ordenDetalle.setFechaCreacion(LocalDateTime.now());
            }
            log.info("✅ [FINALIZADO] Detalles asignados correctamente a la orden: {}", ordenEntity.getNumeroOrden());
        } else {
            log.warn("⚠️ [FINALIZADO] No se encontraron detalles para la orden: {}", ordenEntity.getNumeroOrden());
        }
    }

    /**
     * Agrega detalles a una orden existente y recalcula total.
     */
    public void agregarDetallesOrdenExistente(OrdenEntity ordenEntity, OrdenRequestDTO requestDTO) {
        log.info("🗂️ [SOLICITUD] Agregando detalles a orden existente: {}", ordenEntity.getNumeroOrden());

        if (requestDTO.getDetalles() != null && !requestDTO.getDetalles().isEmpty()) {
            for (var detalleDTO : requestDTO.getDetalles()) {
                OrdenDetalleEntity detalleEntity = mapper.mapDetalleRequestToEntity(detalleDTO);
                detalleEntity.setOrden(ordenEntity);
                detalleEntity.setFechaCreacion(LocalDateTime.now());
                ordenEntity.getDetalles().add(detalleEntity);
            }
        } else {
            log.warn("⚠️ [FINALIZADO] No hay detalles en request para agregar a la orden: {}", ordenEntity.getNumeroOrden());
        }

        mapper.mapDetalleOrderExistente(ordenEntity, requestDTO);

        // Recalcular total despues de agregar nuevos detalles
        recalcularTotalCompra(ordenEntity);

        log.info("✅ [FINALIZADO] Detalles agregados correctamente a orden existente: {}", ordenEntity.getNumeroOrden());
    }

    /**
     * Recalcula el total de la orden sumando todos los subtotales.
     */
    public void recalcularTotalCompra(OrdenEntity ordenEntity) {
        log.info("🧮 [SOLICITUD] Recalculando total de compra para orden: {}", ordenEntity.getNumeroOrden());

        long total = 0L;

        for (OrdenDetalleEntity ordenDetalle : ordenEntity.getDetalles()) {
            long cantidad = (ordenDetalle.getCantidad() != null) ? ordenDetalle.getCantidad() : 0L;
            long precio = (ordenDetalle.getPrecio() != null) ? ordenDetalle.getPrecio() : 0L;

            long subtotal = cantidad * precio;

            total += subtotal;

            log.info("➕ [OPERACION] Sumando subtotal del producto(codigo={}): {} * {} = {}", ordenDetalle.getCodigo(), cantidad, precio, subtotal);
        }

        ordenEntity.setTotalCompra(total);

        log.info("💰 [FINALIZADO] Total de compra recalculado para orden {}: {}", ordenEntity.getNumeroOrden(), total);
    }

    /**
     * Busca un detalle por codigo de producto.
     */
    public OrdenDetalleEntity buscarDetallePorCodigo(OrdenEntity orden, Long codigoProducto) {
        log.info("🔍 [SOLICITUD] Buscando detalle por codigo: {}", codigoProducto);

        if (orden.getDetalles() != null) {
            for (OrdenDetalleEntity detalle : orden.getDetalles()) {
                if (codigoProducto != null && codigoProducto.equals(detalle.getCodigo())) {
                    log.info("🔍 [CONSULTA] Detalle encontrado: {}", codigoProducto);
                    return detalle;
                }
            }
        }

        log.warn("⚠️ [FINALIZADO] No se encontro detalle con codigo: {}", codigoProducto);
        throw new ProductoNoEncontradoException(codigoProducto);
    }

    /**
     * Obtiene la cantidad actual de un detalle.
     */
    public long obtenerCantidadActual(OrdenDetalleEntity detalle) {
        log.info("📊 [SOLICITUD] Consultando cantidad actual del detalle: {}", detalle.getCodigo());

        if (detalle.getCantidad() != null) {
            log.info("✅ [FINALIZADO] Cantidad actual disponible: {}", detalle.getCantidad());
            return detalle.getCantidad();
        } else {
            log.info("⚠️ [RETORNO] Cantidad actual no definida, se retorna 0");
            return 0L;
        }
    }

    /**
     * Cierra la orden y actualiza fecha de modificacion.
     */
    public void mapCerrarOrden(OrdenEntity orden) {
        log.info("🔒 [INICIO] Cerrando orden: {}", orden.getNumeroOrden());

        orden.setEstadoOrden(ESTADO_CERRADA);
        orden.setFechaActualizacion(LocalDateTime.now());

        log.info("✅ [FINALIZADO] Orden cerrada: {}", orden.getNumeroOrden());
    }

    /**
     * Genera el CUFE y lo asigna a la orden al momento de cerrarla.
     * Calcula el IVA (19%) sobre el total de la compra y delega la generacion al CUFEGenerator.
     */
    public void generarAsignarCUFE(OrdenEntity orden) {
        log.info("🔐 [CUFE] Iniciando generacion de CUFE para orden: {}", orden.getNumeroOrden());

        String fechaActual = LocalDateTime.now().toString();

        Long total = orden.getTotalCompra() != null ? orden.getTotalCompra() : 0L;

        // Calcular IVA al 19%
        Long iva = (long) (total * 0.19);

        // Generar CUFE usando el generador de hash SHA-384
        String cufe = CUFEGenerator.generarCUFE(
                orden.getIdentificacionCliente().toString(), // puedes usar NIT real si lo tienes
                orden.getNumeroFactura(),
                fechaActual,
                total.toString(),
                iva.toString(),
                "CLAVE-TECNICA-PRUEBA"
        );

        orden.setCufe(cufe);

        log.info("🔐 [CUFE] CUFE generado y asignado exitosamente a orden: {}", orden.getNumeroOrden());
    }
}
