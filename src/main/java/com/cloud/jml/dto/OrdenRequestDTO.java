package com.cloud.jml.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentoss
public class OrdenRequestDTO {

    private Long codigo;
    private String producto;
    private String descripcion;
    private Long cantidad;
    private Long precio;

    // Relación cliente y empleado
    private Long identificacionCliente;
    private String nombreCliente;
    private Long identificacionEmpleado;
    private String nombreEmpleado;
}
