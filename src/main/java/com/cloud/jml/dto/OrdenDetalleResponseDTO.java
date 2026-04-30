package com.cloud.jml.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos
public class OrdenDetalleResponseDTO {

    private Long codigo;
    private String producto;
    private String descripcion;
    private Long cantidad;
    private Long precio;
    private String fechaCreacion;
    private String fechaActualizacion;
}
