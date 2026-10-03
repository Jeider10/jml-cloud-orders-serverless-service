package com.cloud.jml.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Copia de los detalles (items) de una orden al momento de ser eliminada.
 * Permite restaurar la orden completa con todos sus productos desde la papelera.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordenes_papelera_detalles")
public class OrdenPapeleraDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia al registro de la papelera al que pertenece este detalle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "papelera_id", nullable = false)
    private OrdenEliminadaEntity papelera;

    @Column(name = "codigo_producto")
    private String codigoProducto;

    @Column(name = "nombre_producto", length = 200)
    private String nombreProducto;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "cantidad")
    private Long cantidad;

    @Column(name = "precio")
    private Long precio;
}
