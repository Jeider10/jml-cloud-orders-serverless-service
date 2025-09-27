package com.cloud.jml.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "ordenes_ventas")
public class OrdenEntity {

    @Id
    @Column(name = "numero_orden", nullable = false, unique = true, length = 50)
    private String numeroOrden;

    private String estadoOrden;
    private String numeroFactura;

    private Long identificacionCliente;
    private String nombreCliente;
    private Long identificacionEmpleado;
    private String nombreEmpleado;
    private Long identificacionProveedor;
    private String nombreProveedor;
    private Long totalCompra;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenDetalleEntity> detalles = new ArrayList<>();
}
