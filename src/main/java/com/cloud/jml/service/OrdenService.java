package com.cloud.jml.service;

import com.cloud.jml.dto.OrdenRequestDTO;
import com.cloud.jml.dto.OrdenResponseDTO;
import com.cloud.jml.exception.OrdenDuplicadoException;
import com.cloud.jml.exception.OrdenNoEncontradoException;
import com.cloud.jml.model.OrdenEntity;
import com.cloud.jml.repository.OrdenRepository;
import com.cloud.jml.utils.OrdenMapper;
import com.cloud.jml.utils.OrdenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final OrdenMapper mapper;
    private final OrdenUtils ordenUtils;

    public OrdenService(OrdenRepository ordenRepository, OrdenMapper mapper, OrdenUtils ordenUtils) {
        this.ordenRepository = ordenRepository;
        this.mapper = mapper;
        this.ordenUtils = ordenUtils;
        log.info("🔥 ProductoService inicializado correctamente.");
    }

    @Transactional
    public OrdenResponseDTO crearOrdenDeVenta(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Creando orden de venta para producto: {}", ordenRequestDTO.getProducto());

        Optional<OrdenEntity> existente =
                ordenRepository.findByCodigoAndEstado(ordenRequestDTO.getCodigo(), "ABIERTA");

        OrdenEntity ordenEntity;
        if (existente.isPresent()) {
            // Actualizar orden existente
            ordenEntity = existente.get();
            log.warn("⚠️ Ya existe una orden ABIERTA para el producto {}. Acumulando cantidad.", ordenRequestDTO.getCodigo());

            ordenEntity.setCantidad(ordenEntity.getCantidad() + ordenRequestDTO.getCantidad());
            ordenEntity.setPrecio(ordenRequestDTO.getPrecio());
            ordenEntity.setProducto(ordenRequestDTO.getProducto());
            ordenEntity.setDescripcion(ordenRequestDTO.getDescripcion());
            ordenEntity.setIdentificacionCliente(ordenRequestDTO.getIdentificacionCliente());
            ordenEntity.setNombreCliente(ordenRequestDTO.getNombreCliente());
            ordenEntity.setIdentificacionEmpleado(ordenRequestDTO.getIdentificacionEmpleado());
            ordenEntity.setNombreEmpleado(ordenRequestDTO.getNombreEmpleado());
            ordenEntity.setFechaActualizacion(LocalDateTime.now());

        } else {
            // Crear nueva orden
            ordenEntity = mapper.mapRequestDtoToEntity(ordenRequestDTO);
            ordenEntity.setEstado("ABIERTA");
            ordenEntity.setFechaOrden(LocalDateTime.now());
        }

        OrdenEntity guardado = ordenRepository.save(ordenEntity);
        log.info("✅ Orden procesada correctamente. ID: {}", guardado.getId());

        return mapper.mapEntityToResponseDto(guardado);
    }

    @Transactional
    public OrdenResponseDTO restarCantidadProducto(Long codigo, int cantidadARestar) {
        log.info("📌 Iniciando restarCantidadProducto -> codigo: {}, cantidadARestar: {}", codigo, cantidadARestar);

        // 1) Buscar la orden (registro) por código (no usar orElseThrow)
        Optional<OrdenEntity> ordenOpt = ordenRepository.findFirstByCodigoOrderByFechaCreacionDesc(codigo);

        if (ordenOpt.isEmpty()) {
            log.warn("⚠️ Orden (item) no encontrada con codigo: {}", codigo);
            throw new OrdenNoEncontradoException(codigo); // tu excepción personalizada
        }

        OrdenEntity orden = ordenOpt.get();

        // 2) Validaciones básicas de cantidad a restar
        if (cantidadARestar <= 0) {
            log.warn("⚠️ Intento de restar una cantidad inválida: {} para codigo: {}", cantidadARestar, codigo);
            throw new IllegalArgumentException("Cantidad a restar inválida: " + cantidadARestar);
        }

        Long cantidadActual = orden.getCantidad() == null ? 0L : orden.getCantidad();
        log.info("📌 Cantidad actual en orden (codigo={}): {}", codigo, cantidadActual);

        // 3) Si no hay suficiente cantidad -> error
        if (cantidadActual < cantidadARestar) {
            log.warn("⚠️ Stock insuficiente en la orden. codigo={}, cantidadActual={}, intentoRestar={}",
                    codigo, cantidadActual, cantidadARestar);
            throw new IllegalArgumentException("Stock insuficiente en la orden para restar " + cantidadARestar + " unidades");
        }

        // 4) Calcular nueva cantidad
        long nuevaCantidad = cantidadActual - cantidadARestar;

        // Preparar DTO de respuesta (por si eliminamos, devolvemos una representación con cantidad 0)
        OrdenResponseDTO responseDto;

        if (nuevaCantidad <= 0) {
            // Si queda 0 o menos → eliminar el item de la tabla de órdenes
            // antes de eliminar, crear DTO que refleje la eliminación (cantidad = 0)
            orden.setCantidad(0L);
            orden.setFechaActualizacion(LocalDateTime.now());
            responseDto = mapper.mapEntityToResponseDto(orden);

            ordenRepository.delete(orden);
            log.info("🗑️ Item de orden eliminado (codigo={}) tras restar {} unidades.", codigo, cantidadARestar);

            // Opcional: aquí podrías notificar al micro de productos para incrementar stock
            // try { productoClient.incrementarStock(codigo, cantidadARestar); } catch (Exception e) { log.warn(...); }

            return responseDto;
        } else {
            // Actualizar cantidad y persistir
            orden.setCantidad(nuevaCantidad);
            orden.setFechaActualizacion(LocalDateTime.now());

            OrdenEntity actualizado = ordenRepository.save(orden);
            log.info("✅ Cantidad actualizada en orden: codigo={}, nuevaCantidad={}", codigo, nuevaCantidad);

            // Opcional: notificar al micro de productos que se ha liberado stock (si aplica)
            // try { productoClient.incrementarStock(codigo, cantidadARestar); } catch (Exception e) { log.warn(...); }

            responseDto = mapper.mapEntityToResponseDto(actualizado);
            return responseDto;
        }
    }

    @Transactional
    public OrdenResponseDTO cerrarOrdenPorCliente(Long identificacionCliente) {
        OrdenEntity orden = ordenRepository
                .findFirstByIdentificacionClienteAndEstado(identificacionCliente, "ABIERTA")
                .orElseThrow(() -> new RuntimeException("No existe orden ABIERTA para este cliente"));

        orden.setEstado("CERRADA");
        orden.setFechaActualizacion(LocalDateTime.now());

        OrdenEntity guardado = ordenRepository.save(orden);
        return mapper.mapEntityToResponseDto(guardado);
    }

//    @Transactional(readOnly = true)
//    public List<OrdenResponseDTO> listarProductos() {
//        log.info("📌 Iniciando búsqueda de todos los Productos.");
//
//        // Paso 1: Obtener entidades desde la BD
//        List<OrdenEntity> ordenEntity = ordenRepository.findAll();
//
//        // Paso 2: Convertir a Stream
//        Stream<OrdenEntity> streamProductos = ordenEntity.stream();
//
//        // Paso 3: Mapear cada entidad a DTO
//        Stream<OrdenResponseDTO> streamProductosDTO = streamProductos.map(mapper::mapEntityToResponseDto);
//
//        // Paso 4: Convertir a lista final
//        List<OrdenResponseDTO> productosResponse = streamProductosDTO.toList();
//
//        log.info("📌 Finaliza búsqueda de todos los Productos. Total encontrados: {}", productosResponse.size());
//
//        return productosResponse;
//    }

//    @Transactional
//    public void eliminarProducto(OrdenRequestDTO ordenRequestDTO) {
//        log.info("📌 Iniciando eliminación de Producto con codigo: {}", ordenRequestDTO.getCodigo());
//
//        Optional<OrdenEntity> productoOptional = ordenRepository.findByCodigo(ordenRequestDTO.getCodigo());
//
//        if (productoOptional.isPresent()) {
//            OrdenEntity ordenEntity = productoOptional.get();
//            ordenRepository.delete(ordenEntity);
//            log.info("✅ Producto eliminado con codigo: {}", ordenRequestDTO.getCodigo());
//        } else {
//            log.warn("⚠️ No se encontró el Producto con codigo: {}", ordenRequestDTO.getCodigo());
//            throw new OrdenNoEncontradoException(ordenRequestDTO.getCodigo());
//        }
//    }

//    @Transactional(readOnly = true)
//    public Optional<OrdenResponseDTO> obtenerProductoPorCodigo(Long codigo) {
//        log.info("📌 Iniciando búsqueda de Producto por código: {}", codigo);
//
//        Optional<OrdenEntity> productoEntity = ordenRepository.findByCodigo(codigo);
//
//        if (productoEntity.isPresent()) {
//            Optional<OrdenResponseDTO> productoResponseDTO = productoEntity.map(mapper::mapEntityToResponseDto);
//            log.info("✅ Producto encontrado con código: {}", codigo);
//            return productoResponseDTO;
//        } else {
//            Optional<OrdenResponseDTO> productoResponseDTO = Optional.empty();
//            log.warn("⚠️ Producto no encontrado con código: {}", codigo);
//            return productoResponseDTO;
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public List<OrdenResponseDTO> obtenerProductoPorNombre(OrdenRequestDTO ordenRequestDTO) {
//        log.info("📌 Iniciando búsqueda de Producto por nombre: {}", ordenRequestDTO.getNombre());
//
//        // Paso 1: Buscar entidades por nombre
//        List<OrdenEntity> ordenEntity = ordenRepository.findByNombreContainingIgnoreCase(ordenRequestDTO.getNombre());
//
//        // Paso 2: Validar si está vacío
//        if (ordenEntity.isEmpty()) {
//            log.warn("⚠️ No se encontraron productos con el nombre: {}", ordenRequestDTO.getNombre());
//            return List.of(); // Retorna lista vacía
//        }
//
//        // Paso 3: Convertir a Stream
//        Stream<OrdenEntity> streamProductos = ordenEntity.stream();
//
//        // Paso 4: Mapear cada entidad a DTO
//        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);
//
//        // Paso 5: Convertir a lista final
//        List<OrdenResponseDTO> productosResponse = streamDto.toList();
//
//        log.info("📌 Finaliza búsqueda de Producto por nombre: {}. Total encontrados: {}",
//                ordenRequestDTO.getNombre(), productosResponse.size());
//
//        return productosResponse;
//    }
//
//    @Transactional(readOnly = true)
//    public List<OrdenResponseDTO> obtenerProductoPorDescripcion(OrdenRequestDTO ordenRequestDTO) {
//        log.info("📌 Iniciando búsqueda de Producto por descripcion: {}", ordenRequestDTO.getDescripcion());
//
//        // Paso 1: Buscar entidades por nombre
//        List<OrdenEntity> ordenEntity = ordenRepository.findByDescripcionContainingIgnoreCase(ordenRequestDTO.getDescripcion());
//
//        // Paso 2: Validar si está vacío
//        if (ordenEntity.isEmpty()) {
//            log.warn("⚠️ No se encontraron productos con descripcion: {}", ordenRequestDTO.getDescripcion());
//            return List.of(); // Retorna lista vacía
//        }
//
//        // Paso 3: Convertir a Stream
//        Stream<OrdenEntity> streamProductos = ordenEntity.stream();
//
//        // Paso 4: Mapear cada entidad a DTO
//        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);
//
//        // Paso 5: Convertir a lista final
//        List<OrdenResponseDTO> productosResponse = streamDto.toList();
//
//        log.info("📌 Finaliza búsqueda de Producto por descripcion: {}. Total encontrados: {}",
//                ordenRequestDTO.getDescripcion(), productosResponse.size());
//
//        return productosResponse;
//    }
//
//    @Transactional
//    public List<OrdenResponseDTO> obtenerProductoPorCantidad(OrdenRequestDTO ordenRequestDTO) {
//        log.info("📌 Iniciando búsqueda de Producto por cantidad: {}", ordenRequestDTO.getCantidad());
//
//        // Paso 1: Buscar entidades por nombre
//        List<OrdenEntity> ordenEntity = ordenRepository.findByCantidad(ordenRequestDTO.getCantidad());
//
//        // Paso 2: Validar si está vacío
//        if (ordenEntity.isEmpty()) {
//            log.warn("⚠️ No se encontraron productos con cantidad: {}", ordenRequestDTO.getCantidad());
//            return List.of(); // Retorna lista vacía
//        }
//
//        // Paso 3: Convertir a Stream
//        Stream<OrdenEntity> streamProductos = ordenEntity.stream();
//
//        // Paso 4: Mapear cada entidad a DTO
//        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);
//
//        // Paso 5: Convertir a lista final
//        List<OrdenResponseDTO> productosResponse = streamDto.toList();
//
//        log.info("📌 Finaliza búsqueda de Producto por cantidad: {}. Total encontrados: {}",
//                ordenRequestDTO.getCantidad(), productosResponse.size());
//
//        return productosResponse;
//    }
//
//    @Transactional
//    public List<OrdenResponseDTO> obtenerProductoPorPrecio(OrdenRequestDTO ordenRequestDTO) {
//        log.info("📌 Iniciando búsqueda de Producto por precio: {}", ordenRequestDTO.getPrecio());
//
//        // Paso 1: Buscar entidades por nombre
//        List<OrdenEntity> ordenEntity = ordenRepository.findByPrecio(ordenRequestDTO.getPrecio());
//
//        // Paso 2: Validar si está vacío
//        if (ordenEntity.isEmpty()) {
//            log.warn("⚠️ No se encontraron productos con precio: {}", ordenRequestDTO.getPrecio());
//            return List.of(); // Retorna lista vacía
//        }
//
//        // Paso 3: Convertir a Stream
//        Stream<OrdenEntity> streamProductos = ordenEntity.stream();
//
//        // Paso 4: Mapear cada entidad a DTO
//        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);
//
//        // Paso 5: Convertir a lista final
//        List<OrdenResponseDTO> productosResponse = streamDto.toList();
//
//        log.info("📌 Finaliza búsqueda de Producto por precio: {}. Total encontrados: {}",
//                ordenRequestDTO.getPrecio(), productosResponse.size());
//
//        return productosResponse;
//    }
//
//    @Transactional
//    public List<OrdenResponseDTO> obtenerProductoPorProveedorId(OrdenRequestDTO ordenRequestDTO) {
//        log.info("📌 Iniciando búsqueda de Producto por proveedorId: {}", ordenRequestDTO.getProveedorId());
//
//        // Paso 1: Buscar entidades por nombre
//        List<OrdenEntity> ordenEntity = ordenRepository.findByProveedorId(ordenRequestDTO.getProveedorId());
//
//        // Paso 2: Validar si está vacío
//        if (ordenEntity.isEmpty()) {
//            log.warn("⚠️ No se encontraron productos con proveedorId: {}", ordenRequestDTO.getProveedorId());
//            return List.of(); // Retorna lista vacía
//        }
//
//        // Paso 3: Convertir a Stream
//        Stream<OrdenEntity> streamProductos = ordenEntity.stream();
//
//        // Paso 4: Mapear cada entidad a DTO
//        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);
//
//        // Paso 5: Convertir a lista final
//        List<OrdenResponseDTO> productosResponse = streamDto.toList();
//
//        log.info("📌 Finaliza búsqueda de Producto por proveedorId: {}. Total encontrados: {}",
//                ordenRequestDTO.getProveedorId(), productosResponse.size());
//
//        return productosResponse;
//    }
//
//    @Transactional
//    public List<OrdenResponseDTO> obtenerProductoPorProveedorName(OrdenRequestDTO ordenRequestDTO) {
//        log.info("📌 Iniciando búsqueda de Producto por proveedorName: {}", ordenRequestDTO.getProveedorName());
//
//        // Paso 1: Buscar entidades por nombre
//        List<OrdenEntity> ordenEntity = ordenRepository.findByProveedorNameContainingIgnoreCase(ordenRequestDTO.getProveedorName());
//
//        // Paso 2: Validar si está vacío
//        if (ordenEntity.isEmpty()) {
//            log.warn("⚠️ No se encontraron productos con proveedorName: {}", ordenRequestDTO.getProveedorName());
//            return List.of(); // Retorna lista vacía
//        }
//
//        // Paso 3: Convertir a Stream
//        Stream<OrdenEntity> streamProductos = ordenEntity.stream();
//
//        // Paso 4: Mapear cada entidad a DTO
//        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);
//
//        // Paso 5: Convertir a lista final
//        List<OrdenResponseDTO> productosResponse = streamDto.toList();
//
//        log.info("📌 Finaliza búsqueda de Producto por proveedorName: {}. Total encontrados: {}",
//                ordenRequestDTO.getProveedorName(), productosResponse.size());
//
//        return productosResponse;
//    }
//
//    @Transactional
//    public OrdenResponseDTO actualizarProducto(OrdenRequestDTO ordenRequestDTO) {
//        log.info("📌 Iniciando actualización de Producto con codigo: {}", ordenRequestDTO.getCodigo());
//
//        // Paso 1: Validar existencia
//        OrdenEntity ordenEntity = ordenUtils.validarExistenciaProducto(ordenRequestDTO);
//
//        // Paso 2: Actualizar datos
//        ordenUtils.actualizarDatosProducto(ordenRequestDTO, ordenEntity);
//
//        // Paso 3: Guardar cambios en la BD
//        OrdenEntity actualizado = ordenRepository.save(ordenEntity);
//        log.info("✅ Producto actualizado con código: {}", ordenRequestDTO.getNombre());
//
//        // Paso 4: Mapear a DTO
//        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(actualizado);
//        log.info("📌 Finaliza actualización de Producto: {} con codigo: {}", ordenResponseDTO.getNombre(), ordenResponseDTO.getCodigo());
//
//        return ordenResponseDTO;
//    }

//    @Transactional
//    public List<ProductoResponseDTO> obtenerProductoPorFechaCreacion(ProductoRequestDTO productoRequestDTO) {
//        return productoRepository.findByFechaCreacion(productoResponseDTO.getFechaCreacion())
//                .stream()
//                .map(this::mapEntityToDto)
//                .collect(Collectors.toList());
//    }
//
}
