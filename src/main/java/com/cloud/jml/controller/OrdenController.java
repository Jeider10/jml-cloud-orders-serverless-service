package com.cloud.jml.controller;

import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.service.OrdenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ordenes-ventas")
@CrossOrigin(origins = "http://localhost:8080")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
        log.info("🔥 OrdenController inicializado correctamente.");
    }

    @GetMapping("/list/all")
    public ResponseEntity<List<OrdenResponseDTO>> listarTodasLasOrdenes() {
        log.info("📥 [SOLICITUD] Listar todas las órdenes de venta");

        List<OrdenResponseDTO> ordenes = ordenService.listarTodasLasOrdenes();

        log.info("📤 [RESPUESTA] Se retornan {} órdenes de venta", ordenes.size());

        return ResponseEntity.ok(ordenes);
    }

    @PostMapping("/register")
    public ResponseEntity<OrdenResponseDTO> crearOrdenDeVenta(@RequestBody OrdenRequestDTO ordenRequestDTO) {
        log.info("📥 [SOLICITUD] Creación o actualización de orden de venta para cliente: {}", ordenRequestDTO.getNombreCliente());

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
    public ResponseEntity<OrdenResponseDTO> cerrarOrdenPorCliente(@PathVariable Long identificacionCliente) {
        log.info("📥 [SOLICITUD] Solicitud para cerrar orden del cliente con identificación: {}", identificacionCliente);

        OrdenResponseDTO dto = ordenService.cerrarOrdenPorCliente(identificacionCliente);

        log.info("📤 [RESPUESTA] Orden cerrada exitosamente para cliente con identificación: {}", identificacionCliente);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/list/estado")
    public ResponseEntity<List<OrdenResponseDTO>> listarOrdenesPorEstado(@RequestParam("estado") String estadoOrden) {
        log.info("📥 [SOLICITUD] Solicitud para listar órdenes con estado: {}", estadoOrden);

        List<OrdenResponseDTO> ordenes = ordenService.listarOrdenesPorEstado(estadoOrden);

        log.info("📤 [RESPUESTA] Se retornan {} órdenes con estado: {}", ordenes.size(), estadoOrden);

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/list/cliente")
    public ResponseEntity<List<OrdenResponseDTO>> listarOrdenesPorClienteYEstado(
            @RequestParam("cliente") Long identificacionCliente,
            @RequestParam("estado") String estadoOrden) {

        log.info("📥 [SOLICITUD] Solicitud para listar órdenes del cliente: {} con estado: {}", identificacionCliente, estadoOrden);

        List<OrdenResponseDTO> ordenes = ordenService.listarOrdenesPorClienteYEstado(identificacionCliente, estadoOrden);

        log.info("📤 [RESPUESTA] Se retornan {} órdenes del cliente: {} con estado: {}", ordenes.size(), identificacionCliente, estadoOrden);

        return ResponseEntity.ok(ordenes);
    }

    @DeleteMapping("/delete/{numeroOrden}")
    public ResponseEntity<Void> eliminarOrdenCliente(
            @PathVariable("numeroOrden") String numeroOrden,
            @RequestParam("cliente") Long identificacionCliente) {

        log.info("📥 [SOLICITUD] Solicitud para eliminar orden -> númeroOrden: {}, cliente: {}", numeroOrden, identificacionCliente);

        ordenService.eliminarOrdenCliente(numeroOrden, identificacionCliente);

        log.info("📤 [RESPUESTA] Orden eliminada exitosamente -> númeroOrden: {}, cliente: {}", numeroOrden, identificacionCliente);

        return ResponseEntity.ok().build();
    }
}
