package com.cloud.jml.controller;

import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.exception.OrdenNoEncontradoException;
import com.cloud.jml.service.OrdenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
        log.info("📌 Iniciando petición para agregar Producto: {}", ordenRequestDTO.getProducto());

        OrdenResponseDTO crearOrdenVentaResponse = ordenService.crearOrdenDeVenta(ordenRequestDTO);

        log.info("📌 Finaliza petición para agregar Producto: {}", ordenRequestDTO.getProducto());

        return ResponseEntity.ok(crearOrdenVentaResponse);
    }

    @PatchMapping("/ordenes/{id}/cerrar")
    public ResponseEntity<OrdenResponseDTO> cerrarOrden(@PathVariable Long id) {
        OrdenResponseDTO dto = ordenService.cerrarOrden(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/listar-productos")
    public ResponseEntity<List<OrdenResponseDTO>> listarProductos() {
        log.info("📌 Iniciando petición para listar todos los Productos");

        List<OrdenResponseDTO> productos = ordenService.listarProductos();

        log.info("📌 Finaliza petición para listar todos los Productos");

        return ResponseEntity.ok(productos);
    }

    @PutMapping("/restar/{codigo}")
    public ResponseEntity<OrdenResponseDTO> restarCantidadProducto(@PathVariable("codigo") Long codigo, @RequestParam("cantidad") int cantidadARestar) {

        log.info("📌 Petición RESTAR en orden -> codigo: {}, cantidad: {}", codigo, cantidadARestar);

        OrdenResponseDTO ordenActualizada = ordenService.restarCantidadProducto(codigo, cantidadARestar);

        return ResponseEntity.ok(ordenActualizada);
    }

    @DeleteMapping("/eliminar-codigo")
    public ResponseEntity<Void> eliminarProducto(@RequestParam("codigo") Long codigo) {
        log.info("📌 Iniciando petición para eliminar Producto con codigo: {}", codigo);

        OrdenRequestDTO ordenRequestDTO = new OrdenRequestDTO();
        ordenRequestDTO.setCodigo(codigo);

        try {
            ordenService.eliminarProducto(ordenRequestDTO);
            log.info("📌 Finalizó petición de eliminación de Producto con codigo: {}", ordenRequestDTO.getCodigo());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("⚠️ Error al eliminar Producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

//    @GetMapping("/codigo")
//    public ResponseEntity<OrdenResponseDTO> obtenerProductoPorCodigo(@RequestParam("codigo") Long codigo) {
//        log.info("📌 Iniciando petición para buscar Producto por código: {}", codigo);
//
//        Optional<OrdenResponseDTO> codigoResponse = ordenService.obtenerProductoPorCodigo(codigo);
//
//        ResponseEntity<OrdenResponseDTO> productoCodigoResponse;
//
//        if (codigoResponse.isPresent()) {
//            productoCodigoResponse = ResponseEntity.ok(codigoResponse.get());
//            log.info("📌 Finaliza petición de buscar Producto por código: {}", codigo);
//        } else {
//            productoCodigoResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            log.warn("⚠️ Producto no encontrado con código: {}", codigo);
//        }
//
//        log.info("📌 Finaliza petición de buscar Producto por código: {}", codigo);
//
//        return productoCodigoResponse;
//    }
//
//    @GetMapping("/nombre")
//    public ResponseEntity<List<OrdenResponseDTO>> obtenerProductoPorNombre(@RequestParam("nombre") String nombre) {
//        log.info("📌 Iniciando petición para buscar Producto por nombre: {}", nombre);
//
//        OrdenRequestDTO ordenRequestDTO = new OrdenRequestDTO();
//        ordenRequestDTO.setNombre(nombre);
//
//        List<OrdenResponseDTO> nombreResponse = ordenService.obtenerProductoPorNombre(ordenRequestDTO);
//
//        ResponseEntity<List<OrdenResponseDTO>> productoNombreResponse;
//
//        if (nombreResponse.isEmpty()) {
//            productoNombreResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            log.warn("⚠️ Producto no encontrado con nombre: {}", nombre);
//        } else {
//            productoNombreResponse = ResponseEntity.ok(nombreResponse);
//            log.info("✅ Productos encontrados con nombre: {}. Total: {}", nombre, nombreResponse.size());
//        }
//
//        log.info("📌 Finaliza petición de buscar Producto por nombre: {}", nombre);
//
//        return productoNombreResponse;
//    }
//
//    @GetMapping("/descripcion")
//    public ResponseEntity<List<OrdenResponseDTO>> obtenerProductoPorDescripcion(@RequestParam("descripcion") String descripcion) {
//        log.info("📌 Iniciando petición para buscar Producto por descripcion: {}", descripcion);
//
//        OrdenRequestDTO ordenRequestDTO = new OrdenRequestDTO();
//        ordenRequestDTO.setDescripcion(descripcion);
//
//        List<OrdenResponseDTO> descripcionResponse = ordenService.obtenerProductoPorDescripcion(ordenRequestDTO);
//
//        ResponseEntity<List<OrdenResponseDTO>> productoDescripcionResponse;
//
//        if (descripcionResponse.isEmpty()) {
//            productoDescripcionResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            log.warn("⚠️ Producto no encontrado con descripcion: {}", descripcion);
//        } else {
//            productoDescripcionResponse = ResponseEntity.ok(descripcionResponse);
//            log.info("✅ Productos encontrados con descripcion: {}. Total: {}", descripcion, descripcionResponse.size());
//        }
//
//        log.info("📌 Finaliza petición de buscar Producto por descripcion: {}", descripcion);
//
//        return productoDescripcionResponse;
//    }
//
//    @GetMapping("/cantidad")
//    public ResponseEntity<List<OrdenResponseDTO>> obtenerProductoPorCantidad(@RequestParam("cantidad") Long cantidad) {
//        log.info("📌 Iniciando petición para buscar Producto por cantidad: {}", cantidad);
//
//        OrdenRequestDTO ordenRequestDTO = new OrdenRequestDTO();
//        ordenRequestDTO.setCantidad(cantidad);
//
//        List<OrdenResponseDTO> cantidadResponse = ordenService.obtenerProductoPorCantidad(ordenRequestDTO);
//
//        ResponseEntity<List<OrdenResponseDTO>> productoCantidadResponse;
//
//        if (cantidadResponse.isEmpty()) {
//            productoCantidadResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            log.warn("⚠️ Producto no encontrado con cantidad: {}", cantidad);
//        } else {
//            productoCantidadResponse = ResponseEntity.ok(cantidadResponse);
//            log.info("✅ Productos encontrados con cantidad: {}. Total: {}", cantidad, cantidadResponse.size());
//        }
//
//        log.info("📌 Finaliza petición de buscar Producto por cantidad: {}", cantidad);
//
//        return productoCantidadResponse;
//    }
//
//    @GetMapping("/precio")
//    public ResponseEntity<List<OrdenResponseDTO>> obtenerProductoPorPrecio(@RequestParam("precio") Long precio) {
//        log.info("📌 Iniciando petición para buscar Producto por precio: {}", precio);
//
//        OrdenRequestDTO ordenRequestDTO = new OrdenRequestDTO();
//        ordenRequestDTO.setPrecio(precio);
//
//        List<OrdenResponseDTO> precioResponse = ordenService.obtenerProductoPorPrecio(ordenRequestDTO);
//
//        ResponseEntity<List<OrdenResponseDTO>> productoPrecioResponse;
//
//        if (precioResponse.isEmpty()) {
//            productoPrecioResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            log.warn("⚠️ Producto no encontrado con precio: {}", precio);
//        } else {
//            productoPrecioResponse = ResponseEntity.ok(precioResponse);
//            log.info("✅ Productos encontrados con precio: {}. Total: {}", precio, precioResponse.size());
//        }
//
//        log.info("📌 Finaliza petición de buscar Producto por precio: {}", precio);
//
//        return productoPrecioResponse;
//    }
//
//    @GetMapping("/proveedorId")
//    public ResponseEntity<List<OrdenResponseDTO>> obtenerProductoPorProveedorId(@RequestParam("proveedorId") Long proveedorId) {
//        log.info("📌 Iniciando petición para buscar Producto por proveedorId: {}", proveedorId);
//
//        OrdenRequestDTO productoProveedorIdRequest = new OrdenRequestDTO();
//        productoProveedorIdRequest.setProveedorId(proveedorId);
//
//        List<OrdenResponseDTO> proveedorIdResponse = ordenService.obtenerProductoPorProveedorId(productoProveedorIdRequest);
//
//        ResponseEntity<List<OrdenResponseDTO>> productoProveedorIdResponse;
//
//        if (proveedorIdResponse.isEmpty()) {
//            productoProveedorIdResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            log.warn("⚠️ Producto no encontrado con proveedorId: {}", proveedorId);
//        } else {
//            productoProveedorIdResponse = ResponseEntity.ok(proveedorIdResponse);
//            log.info("✅ Productos encontrados con proveedorId: {}. Total: {}", proveedorId, proveedorIdResponse.size());
//        }
//
//        log.info("📌 Finaliza petición de buscar Producto por proveedorId: {}", proveedorId);
//
//        return productoProveedorIdResponse;
//    }
//
//    @GetMapping("/proveedorName")
//    public ResponseEntity<List<OrdenResponseDTO>> obtenerProductoPorProveedorName(@RequestParam("proveedorName") String proveedorName) {
//        log.info("📌 Iniciando petición para buscar Producto por proveedorName: {}", proveedorName);
//
//        OrdenRequestDTO productoProveedorNameRequest = new OrdenRequestDTO();
//        productoProveedorNameRequest.setProveedorName(proveedorName);
//
//        List<OrdenResponseDTO> proveedorNameResponse = ordenService.obtenerProductoPorProveedorName(productoProveedorNameRequest);
//
//        ResponseEntity<List<OrdenResponseDTO>> productoProveedorNameResponse;
//
//        if (proveedorNameResponse.isEmpty()) {
//            productoProveedorNameResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            log.warn("⚠️ Producto no encontrado con proveedorName: {}", proveedorName);
//        } else {
//            productoProveedorNameResponse = ResponseEntity.ok(proveedorNameResponse);
//            log.info("✅ Productos encontrados con proveedorName: {}. Total: {}", proveedorName, proveedorNameResponse.size());
//        }
//
//        log.info("📌 Finaliza petición de buscar Producto por proveedorName: {}", proveedorName);
//
//        return productoProveedorNameResponse;
//    }
//
//    @PutMapping("/actualizar")
//    public ResponseEntity<OrdenResponseDTO> actualizarProducto(@RequestBody OrdenRequestDTO ordenRequestDTO) {
//        log.info("📌 Iniciando petición para actualizar Producto con codigo: {}", ordenRequestDTO.getCodigo());
//
//        OrdenResponseDTO response;
//
//        try {
//            response = ordenService.actualizarProducto(ordenRequestDTO);
//            log.info("📌 Finaliza petición de actualización de Producto con codigo: {}", ordenRequestDTO.getCodigo());
//            return ResponseEntity.ok(response);
//        } catch (OrdenNoEncontradoException ex) {
//            log.warn("⚠️ No se pudo actualizar el Producto. Código no encontrado: {}", ordenRequestDTO.getCodigo(), ex);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (Exception ex) {
//            log.error("❌ Error al actualizar Producto: {}", ex.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
}
