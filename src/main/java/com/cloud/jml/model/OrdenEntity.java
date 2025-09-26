package com.cloud.jml.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "ordenes_ventas")
public class OrdenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long codigo;

    private String producto;
    private String descripcion;
    private Long cantidad;
    private Long precio;

    private String estado; // ABIERTA o CERRADA
    private LocalDateTime fechaOrden;
    private String numeroFactura;

    // Relación con micro cliente
    private Long identificacionCliente;
    private String nombreCliente;

    // Relación con micro empleado
    private Long identificacionEmpleado;
    private String nombreEmpleado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
