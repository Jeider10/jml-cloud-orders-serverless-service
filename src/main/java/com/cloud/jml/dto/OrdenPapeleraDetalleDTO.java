package com.cloud.jml.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenPapeleraDetalleDTO {

    private Long codigoProducto;
    private String nombreProducto;
    private String descripcion;
    private Long cantidad;
    private Long precio;
}
