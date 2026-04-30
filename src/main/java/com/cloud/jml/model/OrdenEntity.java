package com.cloud.jml.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
    private String apellidoCliente;

    private Long identificacionEmpleado;
    private String nombreEmpleado;
    private String apellidoEmpleado;

    private Long identificacionProveedor;
    private String nombreProveedor;
    private Long totalCompra;

    @Column(name = "cufe", length = 150)
    private String cufe;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relacion uno a muchos: una orden puede tener varios detalles; se propagan cambios y se eliminan huerfanos automaticamente
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenDetalleEntity> detalles = new ArrayList<>();
}
