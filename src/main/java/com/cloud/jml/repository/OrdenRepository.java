package com.cloud.jml.repository;

import com.cloud.jml.model.OrdenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdenRepository extends JpaRepository<OrdenEntity, String> {
    Optional<OrdenEntity> findByNumeroOrden(String numeroOrden);

    Optional<OrdenEntity> findByNumeroOrdenAndEstadoOrden(String numeroOrden, String estadoOrden);

    Optional<OrdenEntity> findFirstByIdentificacionClienteAndEstadoOrden(Long identificacionCliente, String estadoOrden);
}
