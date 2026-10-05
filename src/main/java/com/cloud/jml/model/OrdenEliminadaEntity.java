package com.cloud.jml.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro de auditoria de ordenes eliminadas.
 * Se crea automaticamente antes de eliminar una OrdenEntity.
 * Incluye copia de los detalles para permitir restauracion completa.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordenes_eliminadas")
public class OrdenEliminadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_orden", nullable = false, length = 50)
    private String numeroOrden;

    @Column(name = "estado_orden", length = 20)
    private String estadoOrden;

    @Column(name = "numero_factura", length = 50)
    private String numeroFactura;

    @Column(name = "identificacion_cliente", length = 50)
    private String identificacionCliente;

    @Column(name = "nombre_cliente", length = 200)
    private String nombreCliente;

    @Column(name = "apellido_cliente", length = 200)
    private String apellidoCliente;

    @Column(name = "identificacion_empleado", length = 50)
    private String identificacionEmpleado;

    @Column(name = "nombre_empleado", length = 200)
    private String nombreEmpleado;

    @Column(name = "apellido_empleado", length = 200)
    private String apellidoEmpleado;

    @Column(name = "total_compra")
    private Long totalCompra;

    // ─── Auditoria de eliminacion ───────────────────────────────────────────
    @Column(name = "eliminado_por_identificacion", nullable = false, length = 50)
    private String eliminadoPorIdentificacion;

    @Column(name = "eliminado_por_nombre", length = 200)
    private String eliminadoPorNombre;

    @Column(name = "eliminado_por_rol", length = 50)
    private String eliminadoPorRol;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "fecha_eliminacion", nullable = false)
    private LocalDateTime fechaEliminacion;

    // Fecha limite para eliminacion definitiva (minimo 2 meses desde fecha_eliminacion)
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "fecha_creacion_original")
    private LocalDateTime fechaCreacionOriginal;

    // Detalles (items) de la orden copiados al momento de eliminar — permiten restauracion completa
    @OneToMany(mappedBy = "papelera", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenPapeleraDetalleEntity> detalles = new ArrayList<>();
}
