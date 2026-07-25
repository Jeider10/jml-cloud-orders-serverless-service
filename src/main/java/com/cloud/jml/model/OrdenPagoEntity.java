package com.cloud.jml.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordenes_pagos")
public class OrdenPagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Metodo de pago: EFECTIVO, TARJETA_DEBITO, TARJETA_CREDITO, TRANSFERENCIA, NEQUI
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    // Valor pagado con este metodo
    @Column(name = "valor", nullable = false)
    private Long valor;

    // Referencia opcional (numero de aprobacion, transaccion, etc.)
    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Relacion muchos a uno: varios pagos pertenecen a una misma orden
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numero_orden", nullable = false)
    private OrdenEntity orden;
}
