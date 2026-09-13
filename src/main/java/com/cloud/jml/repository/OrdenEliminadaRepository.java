package com.cloud.jml.repository;

import com.cloud.jml.model.OrdenEliminadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdenEliminadaRepository extends JpaRepository<OrdenEliminadaEntity, Long> {

    List<OrdenEliminadaEntity> findAllByOrderByFechaEliminacionDesc();

    List<OrdenEliminadaEntity> findByEliminadoPorIdentificacionOrderByFechaEliminacionDesc(String identificacion);

    List<OrdenEliminadaEntity> findByNumeroOrdenContainingIgnoreCaseOrderByFechaEliminacionDesc(String numeroOrden);

    // Registros cuya fecha de expiracion ya paso (para limpieza automatica opcional)
    List<OrdenEliminadaEntity> findByFechaExpiracionBefore(LocalDateTime fecha);
}
