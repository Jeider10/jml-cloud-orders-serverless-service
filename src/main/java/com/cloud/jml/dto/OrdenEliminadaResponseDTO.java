package com.cloud.jml.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenEliminadaResponseDTO {

    private Long id;
    private String numeroOrden;
    private String estadoOrden;
    private String numeroFactura;
    private String identificacionCliente;
    private String nombreCliente;
    private String identificacionEmpleado;
    private String nombreEmpleado;
    private Long totalCompra;

    // Auditoria
    private String eliminadoPorIdentificacion;
    private String eliminadoPorNombre;
    private String eliminadoPorRol;
    private String motivo;
    private String fechaEliminacion;
    private String fechaExpiracion;
    private String fechaCreacionOriginal;

    // Detalles de productos para restauracion
    private List<OrdenPapeleraDetalleDTO> detalles;
}
