package com.cloud.jml.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentoss
public class OrdenRequestDTO {

    private Long identificacionCliente;
    private String nombreCliente;
    private String apellidoCliente;
    private Long identificacionEmpleado;
    private String nombreEmpleado;
    private String apellidoEmpleado;
    private Long identificacionProveedor;
    private String nombreProveedor;

    private List<OrdenDetalleRequestDTO> detalles;
}
