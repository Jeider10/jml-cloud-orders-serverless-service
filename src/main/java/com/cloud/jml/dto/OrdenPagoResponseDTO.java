package com.cloud.jml.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenPagoResponseDTO {

    private Long id;
    private String metodoPago;
    private Long valor;
    private String referencia;
    private String fechaCreacion;
}
