package com.cloud.jml.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "ordenes_detalles")
public class OrdenDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // código del producto (identificador dentro del catálogo)
    private Long codigo;
    private String producto;
    private String descripcion;
    private Long cantidad;
    private Long precio;

    // Relación muchos a uno: varios detalles pertenecen a una misma orden; Carga perezosa. Carga diferida para optimizar rendimiento
    @ManyToOne(fetch = FetchType.LAZY) // Solo se trae desde la base de datos cuando realmente accedes al campo orden
    @JoinColumn(name = "numero_orden", nullable = false) // FK hacia OrdenEntity.numero_orden
    private OrdenEntity orden;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
