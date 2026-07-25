package com.cloud.jml.controller;

import com.cloud.jml.dto.CerrarOrdenRequestDTO;
import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.service.OrdenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ordenes-ventas")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
        log.info("🔥 OrdenController inicializado correctamente.");
    }

    @GetMapping("/list/all")
    public ResponseEntity<List<OrdenResponseDTO>> listarTodasLasOrdenes() {
        log.info("📥 [SOLICITUD] Listar todas las ordenes de venta");

        List<OrdenResponseDTO> ordenes = ordenService.listarTodasLasOrdenes();

        log.info("📤 [RESPUESTA] Se retornan {} ordenes de venta", ordenes.size());

        return ResponseEntity.ok(ordenes);
    }

    @PostMapping("/register")
    public ResponseEntity<OrdenResponseDTO> crearOrdenDeVenta(@Valid @RequestBody OrdenRequestDTO ordenRequestDTO) {
        log.info("📥 [SOLICITUD] Creacion o actualizacion de orden de venta para cliente: {}", ordenRequestDTO.getNombreCliente());

        OrdenResponseDTO crearOrdenVentaResponse = ordenService.crearOrdenDeVenta(ordenRequestDTO);

        log.info("📤 [RESPUESTA] Orden procesada exitosamente para cliente: {}", crearOrdenVentaResponse.getNombreCliente());

        return ResponseEntity.ok(crearOrdenVentaResponse);
    }

    @PutMapping("/restar/{numeroOrden}")
    public ResponseEntity<OrdenResponseDTO> restarCantidadProducto(
            @PathVariable("numeroOrden") String numeroOrden,
            @RequestParam("codigo") Long codigoProducto,
            @RequestParam("cantidad") int cantidadARestar) {

        log.info("📥 [SOLICITUD] Restar producto en orden -> numeroOrden: {}, codigoProducto: {}, cantidadARestar: {}", numeroOrden, codigoProducto, cantidadARestar);

        OrdenResponseDTO ordenActualizada = ordenService.restarCantidadProducto(numeroOrden, codigoProducto, cantidadARestar);

        log.info("📤 [RESPUESTA] Orden actualizada exitosamente luego de restar producto -> numeroOrden: {}", numeroOrden);

        return ResponseEntity.ok(ordenActualizada);
    }

    @PatchMapping("/cliente/orden/cerrar/{identificacionCliente}")
    public ResponseEntity<OrdenResponseDTO> cerrarOrdenPorCliente(
            @PathVariable Long identificacionCliente,
            @RequestParam(value = "valorRecibido", required = false, defaultValue = "0") Long valorRecibido) {

        log.info("📥 [SOLICITUD] Solicitud para cerrar orden del cliente con identificacion: {} | valorRecibido: {}", identificacionCliente, valorRecibido);

        OrdenResponseDTO dto = ordenService.cerrarOrdenPorCliente(identificacionCliente, valorRecibido);

        log.info("📤 [RESPUESTA] Orden cerrada exitosamente para cliente con identificacion: {}", identificacionCliente);

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/cliente/orden/cerrar-v2")
    public ResponseEntity<OrdenResponseDTO> cerrarOrdenConPagosMixtos(
            @Valid @RequestBody CerrarOrdenRequestDTO cerrarRequest) {

        log.info("📥 [SOLICITUD] Cerrar orden con pagos mixtos para cliente: {} | pagos: {}",
                cerrarRequest.getIdentificacionCliente(),
                cerrarRequest.getPagos() != null ? cerrarRequest.getPagos().size() : 0);

        OrdenResponseDTO dto = ordenService.cerrarOrdenConPagosMixtos(cerrarRequest);

        log.info("📤 [RESPUESTA] Orden cerrada exitosamente con pagos mixtos para cliente: {}", cerrarRequest.getIdentificacionCliente());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/list/estado")
    public ResponseEntity<List<OrdenResponseDTO>> listarOrdenesPorEstado(@RequestParam("estado") String estadoOrden) {
        log.info("📥 [SOLICITUD] Solicitud para listar ordenes con estado: {}", estadoOrden);

        List<OrdenResponseDTO> ordenes = ordenService.listarOrdenesPorEstado(estadoOrden);

        log.info("📤 [RESPUESTA] Se retornan {} ordenes con estado: {}", ordenes.size(), estadoOrden);

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/list/cliente")
    public ResponseEntity<List<OrdenResponseDTO>> listarOrdenesPorClienteYEstado(
            @RequestParam("cliente") Long identificacionCliente,
            @RequestParam("estado") String estadoOrden) {

        log.info("📥 [SOLICITUD] Solicitud para listar ordenes del cliente: {} con estado: {}", identificacionCliente, estadoOrden);

        List<OrdenResponseDTO> ordenes = ordenService.listarOrdenesPorClienteYEstado(identificacionCliente, estadoOrden);

        log.info("📤 [RESPUESTA] Se retornan {} ordenes del cliente: {} con estado: {}", ordenes.size(), identificacionCliente, estadoOrden);

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/fechaCreacion")
    public ResponseEntity<List<OrdenResponseDTO>> obtenerOrdenesPorFechaCreacion(
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam("fechaFin") String fechaFin) {

        log.info("📥 [SOLICITUD] Buscar ordenes por rango de fecha de creacion: {} - {}", fechaInicio, fechaFin);

        List<OrdenResponseDTO> ordenesFecha = ordenService.obtenerOrdenesPorFechaCreacion(fechaInicio, fechaFin);

        if (ordenesFecha == null || ordenesFecha.isEmpty()) {
            log.warn("⚠️ [RESPUESTA] No se encontraron ordenes en el rango de fechas.");
            return ResponseEntity.noContent().build();
        }

        log.info("📤 [RESPUESTA] Se retornan {} ordenes en el rango de fechas.", ordenesFecha.size());

        return ResponseEntity.ok(ordenesFecha);
    }

    @DeleteMapping("/delete/{numeroOrden}")
    public ResponseEntity<Void> eliminarOrdenCliente(
            @PathVariable String numeroOrden,
            @RequestParam("cliente") Long identificacionCliente) {

        log.info("📥 [SOLICITUD] Solicitud para eliminar orden -> numeroOrden: {}, cliente: {}", numeroOrden, identificacionCliente);

        ordenService.eliminarOrdenCliente(numeroOrden, identificacionCliente);

        log.info("📤 [RESPUESTA] Orden eliminada exitosamente -> numeroOrden: {}, cliente: {}", numeroOrden, identificacionCliente);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<OrdenResponseDTO>> buscarOrdenes(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "cliente", required = false) String cliente,
            @RequestParam(value = "idCliente", required = false) Long idCliente,
            @RequestParam(value = "vendedor", required = false) String vendedor,
            @RequestParam(value = "idVendedor", required = false) Long idVendedor,
            @RequestParam(value = "factura", required = false) String factura,
            @RequestParam(value = "producto", required = false) String producto,
            @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
            @RequestParam(value = "fechaFin", required = false) String fechaFin) {

        log.info("📥 [SOLICITUD] Buscar ordenes con filtros -> estado={}, cliente={}, idCliente={}, vendedor={}, idVendedor={}, factura={}, producto={}, fechas={}/{}",
                estado, cliente, idCliente, vendedor, idVendedor, factura, producto, fechaInicio, fechaFin);

        List<OrdenResponseDTO> ordenes = ordenService.buscarOrdenes(estado, cliente, idCliente, vendedor, idVendedor, factura, producto, fechaInicio, fechaFin);

        if (ordenes == null || ordenes.isEmpty()) {
            log.warn("⚠️ [RESPUESTA] No se encontraron ordenes con los filtros proporcionados.");
            return ResponseEntity.noContent().build();
        }

        log.info("📤 [RESPUESTA] Se retornan {} ordenes con los filtros aplicados.", ordenes.size());

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/buscar/cliente")
    public ResponseEntity<List<OrdenResponseDTO>> buscarPorCliente(@RequestParam("cliente") String nombreCliente) {
        log.info("📥 [SOLICITUD] Buscar ordenes por nombre de cliente: {}", nombreCliente);

        List<OrdenResponseDTO> ordenes = ordenService.buscarPorNombreCliente(nombreCliente);

        if (ordenes == null || ordenes.isEmpty()) {
            log.warn("⚠️ [RESPUESTA] No se encontraron ordenes para el cliente: {}", nombreCliente);
            return ResponseEntity.noContent().build();
        }

        log.info("📤 [RESPUESTA] Se retornan {} ordenes para cliente: {}", ordenes.size(), nombreCliente);

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/buscar/idCliente")
    public ResponseEntity<List<OrdenResponseDTO>> buscarPorIdCliente(@RequestParam("idCliente") Long idCliente) {
        log.info("📥 [SOLICITUD] Buscar ordenes por ID cliente: {}", idCliente);

        List<OrdenResponseDTO> ordenes = ordenService.buscarPorIdCliente(idCliente);

        if (ordenes == null || ordenes.isEmpty()) {
            log.warn("⚠️ [RESPUESTA] No se encontraron ordenes para el ID cliente: {}", idCliente);
            return ResponseEntity.noContent().build();
        }

        log.info("📤 [RESPUESTA] Se retornan {} ordenes para ID cliente: {}", ordenes.size(), idCliente);

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/buscar/vendedor")
    public ResponseEntity<List<OrdenResponseDTO>> buscarPorVendedor(@RequestParam("vendedor") String nombreVendedor) {
        log.info("📥 [SOLICITUD] Buscar ordenes por vendedor: {}", nombreVendedor);

        List<OrdenResponseDTO> ordenes = ordenService.buscarPorNombreVendedor(nombreVendedor);

        if (ordenes == null || ordenes.isEmpty()) {
            log.warn("⚠️ [RESPUESTA] No se encontraron ordenes para el vendedor: {}", nombreVendedor);
            return ResponseEntity.noContent().build();
        }

        log.info("📤 [RESPUESTA] Se retornan {} ordenes para vendedor: {}", ordenes.size(), nombreVendedor);

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/buscar/idVendedor")
    public ResponseEntity<List<OrdenResponseDTO>> buscarPorIdVendedor(@RequestParam("idVendedor") Long idVendedor) {
        log.info("📥 [SOLICITUD] Buscar ordenes por ID vendedor: {}", idVendedor);

        List<OrdenResponseDTO> ordenes = ordenService.buscarPorIdVendedor(idVendedor);

        if (ordenes == null || ordenes.isEmpty()) {
            log.warn("⚠️ [RESPUESTA] No se encontraron ordenes para el ID vendedor: {}", idVendedor);
            return ResponseEntity.noContent().build();
        }

        log.info("📤 [RESPUESTA] Se retornan {} ordenes para ID vendedor: {}", ordenes.size(), idVendedor);

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/buscar/factura")
    public ResponseEntity<List<OrdenResponseDTO>> buscarPorFactura(@RequestParam("factura") String factura) {
        log.info("📥 [SOLICITUD] Buscar ordenes por numero de factura: {}", factura);

        List<OrdenResponseDTO> ordenes = ordenService.buscarPorNumeroFactura(factura);

        if (ordenes == null || ordenes.isEmpty()) {
            log.warn("⚠️ [RESPUESTA] No se encontraron ordenes con numero de factura: {}", factura);
            return ResponseEntity.noContent().build();
        }

        log.info("📤 [RESPUESTA] Se retornan {} ordenes para factura: {}", ordenes.size(), factura);

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/buscar/producto")
    public ResponseEntity<List<OrdenResponseDTO>> buscarPorProducto(@RequestParam("producto") String producto) {
        log.info("📥 [SOLICITUD] Buscar ordenes por producto: {}", producto);

        List<OrdenResponseDTO> ordenes = ordenService.buscarPorProducto(producto);

        if (ordenes == null || ordenes.isEmpty()) {
            log.warn("⚠️ [RESPUESTA] No se encontraron ordenes con producto: {}", producto);
            return ResponseEntity.noContent().build();
        }

        log.info("📤 [RESPUESTA] Se retornan {} ordenes con producto: {}", ordenes.size(), producto);

        return ResponseEntity.ok(ordenes);
    }
}
