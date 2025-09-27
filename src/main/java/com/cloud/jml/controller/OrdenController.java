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
    }

    @PostMapping("/register")
    public ResponseEntity<OrdenResponseDTO> crearOrdenDeVenta(@RequestBody OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando petición para crear/actualizar Orden de Venta para cliente: {}", ordenRequestDTO.getIdentificacionCliente());

        OrdenResponseDTO crearOrdenVentaResponse = ordenService.crearOrdenDeVenta(ordenRequestDTO);

        log.info("📌 Finaliza petición para crear/actualizar Orden de Venta para cliente: {}", ordenRequestDTO.getIdentificacionCliente());

        return ResponseEntity.ok(crearOrdenVentaResponse);
    }

    @PutMapping("/restar/{numeroOrden}")
    public ResponseEntity<OrdenResponseDTO> restarCantidadProducto(
            @PathVariable("numeroOrden") String numeroOrden,
            @RequestParam("codigo") Long codigoProducto,
            @RequestParam("cantidad") int cantidadARestar) {

        log.info("📌 Petición RESTAR en orden -> numeroOrden: {}, codigoProducto: {}, cantidad: {}", numeroOrden, codigoProducto, cantidadARestar);

        OrdenResponseDTO ordenActualizada = ordenService.restarCantidadProducto(numeroOrden, codigoProducto, cantidadARestar);

        log.info("📌 Finaliza petición para restar/eliminar producto de venta para cliente: {}", cantidadARestar);

        return ResponseEntity.ok(ordenActualizada);
    }

    @PatchMapping("/cliente/orden/cerrar/{identificacionCliente}")
    public ResponseEntity<OrdenResponseDTO> cerrarOrdenPorCliente(@PathVariable Long identificacionCliente) {
        log.info("📌 Iniciando petición para cerrar cuenta del cliente: {}", identificacionCliente);

        OrdenResponseDTO dto = ordenService.cerrarOrdenPorCliente(identificacionCliente);

        log.info("📌 Finaliza petición para cerrar venta para cliente con identificacion: {}", identificacionCliente);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/list/estado")
    public ResponseEntity<List<OrdenResponseDTO>> listarOrdenesPorEstado(@RequestParam("estado") String estadoOrden) {
        log.info("📌 Iniciando petición para listar ordenes por estado: {}", estadoOrden);

        List<OrdenResponseDTO> ordenes = ordenService.listarOrdenesPorEstado(estadoOrden);

        log.info("📌 Finaliza petición para listar ordenes por estado: {}", estadoOrden);

        return ResponseEntity.ok(ordenes);
    }

    @GetMapping("/list/cliente")
    public ResponseEntity<List<OrdenResponseDTO>> listarOrdenesPorClienteYEstado(
            @RequestParam("cliente") Long identificacionCliente,
            @RequestParam("estado") String estadoOrden) {

        log.info("📌 Iniciando petición para listar ordenes por cliente: {}, y estado: {}", identificacionCliente, estadoOrden);

        List<OrdenResponseDTO> ordenes = ordenService.listarOrdenesPorClienteYEstado(identificacionCliente, estadoOrden);

        log.info("📌 Finaliza petición para listar ordenes por cliente: {}, y estado: {}", identificacionCliente, estadoOrden);

        return ResponseEntity.ok(ordenes);
    }
}
