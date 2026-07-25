package com.cloud.jml.repository;

import com.cloud.jml.model.OrdenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<OrdenEntity, String> {

    Optional<OrdenEntity> findFirstByIdentificacionClienteAndEstadoOrden(Long identificacionCliente, String estadoOrden);

    Optional<OrdenEntity> findByNumeroOrdenAndEstadoOrden(String numeroOrden, String estadoOrden);

    List<OrdenEntity> findByEstadoOrden(String estadoOrden);

    List<OrdenEntity> findByEstadoOrdenOrderByFechaCreacionAsc(String estadoOrden);

    List<OrdenEntity> findByIdentificacionClienteAndEstadoOrden(Long identificacionCliente, String estadoOrden);

    Optional<OrdenEntity> findByNumeroOrdenAndIdentificacionCliente(String numeroOrden, Long identificacionCliente);

    List<OrdenEntity> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    List<OrdenEntity> findAllByOrderByFechaCreacionAsc();

    List<OrdenEntity> findByFechaCreacionBetweenOrderByFechaCreacionAsc(LocalDateTime inicio, LocalDateTime fin);

    List<OrdenEntity> findByNombreClienteContainingIgnoreCaseOrderByFechaCreacionAsc(String nombreCliente);

    List<OrdenEntity> findByIdentificacionClienteOrderByFechaCreacionAsc(Long identificacionCliente);

    List<OrdenEntity> findByNombreEmpleadoContainingIgnoreCaseOrderByFechaCreacionAsc(String nombreEmpleado);

    List<OrdenEntity> findByIdentificacionEmpleadoOrderByFechaCreacionAsc(Long identificacionEmpleado);

    List<OrdenEntity> findByNumeroFacturaContainingIgnoreCaseOrderByFechaCreacionAsc(String numeroFactura);
}
