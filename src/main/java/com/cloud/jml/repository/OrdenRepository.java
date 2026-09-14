package com.cloud.jml.repository;

import com.cloud.jml.model.OrdenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<OrdenEntity, String> {

    // identificacionCliente es ahora String (soporta "CONSUMIDOR FINAL" y numeros)
    Optional<OrdenEntity> findFirstByIdentificacionClienteAndEstadoOrden(String identificacionCliente, String estadoOrden);

    Optional<OrdenEntity> findByNumeroOrdenAndEstadoOrden(String numeroOrden, String estadoOrden);

    Optional<OrdenEntity> findByNumeroOrdenAndEstadoOrdenIn(String numeroOrden, List<String> estados);

    List<OrdenEntity> findByEstadoOrden(String estadoOrden);

    List<OrdenEntity> findByEstadoOrdenOrderByFechaCreacionAsc(String estadoOrden);

    List<OrdenEntity> findByIdentificacionClienteAndEstadoOrden(String identificacionCliente, String estadoOrden);

    Optional<OrdenEntity> findByNumeroOrdenAndIdentificacionCliente(String numeroOrden, String identificacionCliente);

    List<OrdenEntity> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    List<OrdenEntity> findAllByOrderByFechaCreacionAsc();

    List<OrdenEntity> findByFechaCreacionBetweenOrderByFechaCreacionAsc(LocalDateTime inicio, LocalDateTime fin);

    List<OrdenEntity> findByNombreClienteContainingIgnoreCaseOrderByFechaCreacionAsc(String nombreCliente);

    List<OrdenEntity> findByIdentificacionClienteOrderByFechaCreacionAsc(String identificacionCliente);

    List<OrdenEntity> findByNombreEmpleadoContainingIgnoreCaseOrderByFechaCreacionAsc(String nombreEmpleado);

    List<OrdenEntity> findByIdentificacionEmpleadoOrderByFechaCreacionAsc(String identificacionEmpleado);

    List<OrdenEntity> findByNumeroFacturaContainingIgnoreCaseOrderByFechaCreacionAsc(String numeroFactura);
}
