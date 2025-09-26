package com.cloud.jml.repository;

import com.cloud.jml.model.OrdenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<OrdenEntity, Long> {
    Optional<OrdenEntity> findByCodigo(Long codigo);

    List<OrdenEntity> findByNombre(String nombre);

    List<OrdenEntity> findByNombreContainingIgnoreCase(String nombre);

    List<OrdenEntity> findByDescripcion(String descripcion);

    List<OrdenEntity> findByDescripcionContainingIgnoreCase(String descripcion);

    List<OrdenEntity> findByCantidad(Long cantidad);

    List<OrdenEntity> findByPrecio(Long precio);

    List<OrdenEntity> findByProveedorId(Long proveedorId);

    List<OrdenEntity> findByProveedorName(String proveedorName);

    List<OrdenEntity> findByProveedorNameContainingIgnoreCase(String proveedorName);

    List<OrdenEntity> findByFechaCreacion(LocalDateTime fechaCreacion);
}
