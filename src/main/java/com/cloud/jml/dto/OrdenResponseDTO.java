package com.cloud.jml.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos
public class OrdenResponseDTO {

    private Long codigo;
    private String producto;
    private String descripcion;
    private Long cantidad;
    private Long precio;
    private String fechaCreacion;
    private String fechaActualizacion;

    // Estado y extras
    private String estado;
    private String fechaOrden;
    private String numeroFactura;

    // Relación cliente y empleado
    private Long identificacionCliente;
    private String nombreCliente;
    private Long identificacionEmpleado;
    private String nombreEmpleado;
}
