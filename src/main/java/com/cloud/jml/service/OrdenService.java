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
    public OrdenResponseDTO crearProducto(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Inicio de creación de Producto: {}", ordenRequestDTO.getNombre());

        // Verificar si ya existe por codigo
        Optional<OrdenEntity> existente = ordenRepository.findByCodigo(ordenRequestDTO.getCodigo());
        if (existente.isPresent()) {
            log.warn("⚠️ Producto duplicado: {}", ordenRequestDTO.getCodigo());
            throw new OrdenDuplicadoException(ordenRequestDTO.getCodigo());
        }

        // Mapeo de DTO a Entity
        OrdenEntity ordenEntity = mapper.mapRequestDtoToEntity(ordenRequestDTO);

        // Guardamos en la base de datos
        OrdenEntity guardado = ordenRepository.save(ordenEntity);
        log.info("✅ Producto creado correctamente: {}", ordenRequestDTO.getNombre());

        // Convertimos de nuevo a DTO
        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(guardado);
        log.info("📌 Finaliza creación de Producto: {}", ordenResponseDTO.getNombre());

        return ordenResponseDTO;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> listarProductos() {
        log.info("📌 Iniciando búsqueda de todos los Productos.");

        // Paso 1: Obtener entidades desde la BD
        List<OrdenEntity> ordenEntity = ordenRepository.findAll();

        // Paso 2: Convertir a Stream
        Stream<OrdenEntity> streamProductos = ordenEntity.stream();

        // Paso 3: Mapear cada entidad a DTO
        Stream<OrdenResponseDTO> streamProductosDTO = streamProductos.map(mapper::mapEntityToResponseDto);

        // Paso 4: Convertir a lista final
        List<OrdenResponseDTO> productosResponse = streamProductosDTO.toList();

        log.info("📌 Finaliza búsqueda de todos los Productos. Total encontrados: {}", productosResponse.size());

        return productosResponse;
    }

    @Transactional(readOnly = true)
    public Optional<OrdenResponseDTO> obtenerProductoPorCodigo(Long codigo) {
        log.info("📌 Iniciando búsqueda de Producto por código: {}", codigo);

        Optional<OrdenEntity> productoEntity = ordenRepository.findByCodigo(codigo);

        if (productoEntity.isPresent()) {
            Optional<OrdenResponseDTO> productoResponseDTO = productoEntity.map(mapper::mapEntityToResponseDto);
            log.info("✅ Producto encontrado con código: {}", codigo);
            return productoResponseDTO;
        } else {
            Optional<OrdenResponseDTO> productoResponseDTO = Optional.empty();
            log.warn("⚠️ Producto no encontrado con código: {}", codigo);
            return productoResponseDTO;
        }
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> obtenerProductoPorNombre(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando búsqueda de Producto por nombre: {}", ordenRequestDTO.getNombre());

        // Paso 1: Buscar entidades por nombre
        List<OrdenEntity> ordenEntity = ordenRepository.findByNombreContainingIgnoreCase(ordenRequestDTO.getNombre());

        // Paso 2: Validar si está vacío
        if (ordenEntity.isEmpty()) {
            log.warn("⚠️ No se encontraron productos con el nombre: {}", ordenRequestDTO.getNombre());
            return List.of(); // Retorna lista vacía
        }

        // Paso 3: Convertir a Stream
        Stream<OrdenEntity> streamProductos = ordenEntity.stream();

        // Paso 4: Mapear cada entidad a DTO
        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);

        // Paso 5: Convertir a lista final
        List<OrdenResponseDTO> productosResponse = streamDto.toList();

        log.info("📌 Finaliza búsqueda de Producto por nombre: {}. Total encontrados: {}",
                ordenRequestDTO.getNombre(), productosResponse.size());

        return productosResponse;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponseDTO> obtenerProductoPorDescripcion(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando búsqueda de Producto por descripcion: {}", ordenRequestDTO.getDescripcion());

        // Paso 1: Buscar entidades por nombre
        List<OrdenEntity> ordenEntity = ordenRepository.findByDescripcionContainingIgnoreCase(ordenRequestDTO.getDescripcion());

        // Paso 2: Validar si está vacío
        if (ordenEntity.isEmpty()) {
            log.warn("⚠️ No se encontraron productos con descripcion: {}", ordenRequestDTO.getDescripcion());
            return List.of(); // Retorna lista vacía
        }

        // Paso 3: Convertir a Stream
        Stream<OrdenEntity> streamProductos = ordenEntity.stream();

        // Paso 4: Mapear cada entidad a DTO
        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);

        // Paso 5: Convertir a lista final
        List<OrdenResponseDTO> productosResponse = streamDto.toList();

        log.info("📌 Finaliza búsqueda de Producto por descripcion: {}. Total encontrados: {}",
                ordenRequestDTO.getDescripcion(), productosResponse.size());

        return productosResponse;
    }

    @Transactional
    public List<OrdenResponseDTO> obtenerProductoPorCantidad(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando búsqueda de Producto por cantidad: {}", ordenRequestDTO.getCantidad());

        // Paso 1: Buscar entidades por nombre
        List<OrdenEntity> ordenEntity = ordenRepository.findByCantidad(ordenRequestDTO.getCantidad());

        // Paso 2: Validar si está vacío
        if (ordenEntity.isEmpty()) {
            log.warn("⚠️ No se encontraron productos con cantidad: {}", ordenRequestDTO.getCantidad());
            return List.of(); // Retorna lista vacía
        }

        // Paso 3: Convertir a Stream
        Stream<OrdenEntity> streamProductos = ordenEntity.stream();

        // Paso 4: Mapear cada entidad a DTO
        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);

        // Paso 5: Convertir a lista final
        List<OrdenResponseDTO> productosResponse = streamDto.toList();

        log.info("📌 Finaliza búsqueda de Producto por cantidad: {}. Total encontrados: {}",
                ordenRequestDTO.getCantidad(), productosResponse.size());

        return productosResponse;
    }

    @Transactional
    public List<OrdenResponseDTO> obtenerProductoPorPrecio(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando búsqueda de Producto por precio: {}", ordenRequestDTO.getPrecio());

        // Paso 1: Buscar entidades por nombre
        List<OrdenEntity> ordenEntity = ordenRepository.findByPrecio(ordenRequestDTO.getPrecio());

        // Paso 2: Validar si está vacío
        if (ordenEntity.isEmpty()) {
            log.warn("⚠️ No se encontraron productos con precio: {}", ordenRequestDTO.getPrecio());
            return List.of(); // Retorna lista vacía
        }

        // Paso 3: Convertir a Stream
        Stream<OrdenEntity> streamProductos = ordenEntity.stream();

        // Paso 4: Mapear cada entidad a DTO
        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);

        // Paso 5: Convertir a lista final
        List<OrdenResponseDTO> productosResponse = streamDto.toList();

        log.info("📌 Finaliza búsqueda de Producto por precio: {}. Total encontrados: {}",
                ordenRequestDTO.getPrecio(), productosResponse.size());

        return productosResponse;
    }

    @Transactional
    public List<OrdenResponseDTO> obtenerProductoPorProveedorId(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando búsqueda de Producto por proveedorId: {}", ordenRequestDTO.getProveedorId());

        // Paso 1: Buscar entidades por nombre
        List<OrdenEntity> ordenEntity = ordenRepository.findByProveedorId(ordenRequestDTO.getProveedorId());

        // Paso 2: Validar si está vacío
        if (ordenEntity.isEmpty()) {
            log.warn("⚠️ No se encontraron productos con proveedorId: {}", ordenRequestDTO.getProveedorId());
            return List.of(); // Retorna lista vacía
        }

        // Paso 3: Convertir a Stream
        Stream<OrdenEntity> streamProductos = ordenEntity.stream();

        // Paso 4: Mapear cada entidad a DTO
        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);

        // Paso 5: Convertir a lista final
        List<OrdenResponseDTO> productosResponse = streamDto.toList();

        log.info("📌 Finaliza búsqueda de Producto por proveedorId: {}. Total encontrados: {}",
                ordenRequestDTO.getProveedorId(), productosResponse.size());

        return productosResponse;
    }

    @Transactional
    public List<OrdenResponseDTO> obtenerProductoPorProveedorName(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando búsqueda de Producto por proveedorName: {}", ordenRequestDTO.getProveedorName());

        // Paso 1: Buscar entidades por nombre
        List<OrdenEntity> ordenEntity = ordenRepository.findByProveedorNameContainingIgnoreCase(ordenRequestDTO.getProveedorName());

        // Paso 2: Validar si está vacío
        if (ordenEntity.isEmpty()) {
            log.warn("⚠️ No se encontraron productos con proveedorName: {}", ordenRequestDTO.getProveedorName());
            return List.of(); // Retorna lista vacía
        }

        // Paso 3: Convertir a Stream
        Stream<OrdenEntity> streamProductos = ordenEntity.stream();

        // Paso 4: Mapear cada entidad a DTO
        Stream<OrdenResponseDTO> streamDto = streamProductos.map(mapper::mapEntityToResponseDto);

        // Paso 5: Convertir a lista final
        List<OrdenResponseDTO> productosResponse = streamDto.toList();

        log.info("📌 Finaliza búsqueda de Producto por proveedorName: {}. Total encontrados: {}",
                ordenRequestDTO.getProveedorName(), productosResponse.size());

        return productosResponse;
    }

    @Transactional
    public OrdenResponseDTO actualizarProducto(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando actualización de Producto con codigo: {}", ordenRequestDTO.getCodigo());

        // Paso 1: Validar existencia
        OrdenEntity ordenEntity = ordenUtils.validarExistenciaProducto(ordenRequestDTO);

        // Paso 2: Actualizar datos
        ordenUtils.actualizarDatosProducto(ordenRequestDTO, ordenEntity);

        // Paso 3: Guardar cambios en la BD
        OrdenEntity actualizado = ordenRepository.save(ordenEntity);
        log.info("✅ Producto actualizado con código: {}", ordenRequestDTO.getNombre());

        // Paso 4: Mapear a DTO
        OrdenResponseDTO ordenResponseDTO = mapper.mapEntityToResponseDto(actualizado);
        log.info("📌 Finaliza actualización de Producto: {} con codigo: {}", ordenResponseDTO.getNombre(), ordenResponseDTO.getCodigo());

        return ordenResponseDTO;
    }

    @Transactional
    public void eliminarProducto(OrdenRequestDTO ordenRequestDTO) {
        log.info("📌 Iniciando eliminación de Producto con codigo: {}", ordenRequestDTO.getCodigo());

        Optional<OrdenEntity> productoOptional = ordenRepository.findByCodigo(ordenRequestDTO.getCodigo());

        if (productoOptional.isPresent()) {
            OrdenEntity ordenEntity = productoOptional.get();
            ordenRepository.delete(ordenEntity);
            log.info("✅ Producto eliminado con codigo: {}", ordenRequestDTO.getCodigo());
        } else {
            log.warn("⚠️ No se encontró el Producto con codigo: {}", ordenRequestDTO.getCodigo());
            throw new OrdenNoEncontradoException(ordenRequestDTO.getCodigo());
        }
    }

//    @Transactional
//    public List<ProductoResponseDTO> obtenerProductoPorFechaCreacion(ProductoRequestDTO productoRequestDTO) {
//        return productoRepository.findByFechaCreacion(productoResponseDTO.getFechaCreacion())
//                .stream()
//                .map(this::mapEntityToDto)
//                .collect(Collectors.toList());
//    }
//
}
