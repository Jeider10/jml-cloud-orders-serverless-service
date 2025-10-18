package com.cloud.jml.repository;

import com.cloud.jml.model.OrdenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<OrdenEntity, String> {

    Optional<OrdenEntity> findFirstByIdentificacionClienteAndEstadoOrden(Long identificacionCliente, String estadoOrden);

    Optional<OrdenEntity> findByNumeroOrdenAndEstadoOrden(String numeroOrden, String estadoOrden);

    List<OrdenEntity> findByEstadoOrden(String estadoOrden);

    List<OrdenEntity> findByIdentificacionClienteAndEstadoOrden(Long identificacionCliente, String estadoOrden);

    Optional<OrdenEntity> findByNumeroOrdenAndIdentificacionCliente(String numeroOrden, Long identificacionCliente);
}
