package com.cloud.jml.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos
public class OrdenResponseDTO {

    private String numeroOrden;
    private String estadoOrden;
    private String numeroFactura;
    private String fechaCreacion;
    private String fechaActualizacion;

    // Relación cliente, empleado y proveedor
    private Long identificacionCliente;
    private String nombreCliente;
    private Long identificacionEmpleado;
    private String nombreEmpleado;
    private Long identificacionProveedor;
    private String nombreProveedor;

    private List<OrdenDetalleResponseDTO> detalles;
}
