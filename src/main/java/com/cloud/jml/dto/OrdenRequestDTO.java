package com.cloud.jml.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos
public class OrdenRequestDTO {

    // identificacionCliente es opcional — si no se proporciona se usa "CONSUMIDOR FINAL"
    private String identificacionCliente;

    // @NotBlank(message = "El campo 'nombreCliente' es obligatorio")
    @Size(max = 100, message = "El campo 'nombreCliente' no puede exceder 100 caracteres")
    private String nombreCliente;

    // @NotBlank(message = "El campo 'apellidoCliente' es obligatorio")
    @Size(max = 100, message = "El campo 'apellidoCliente' no puede exceder 100 caracteres")
    private String apellidoCliente;

    private String identificacionEmpleado;

    @Size(max = 100, message = "El campo 'nombreEmpleado' no puede exceder 100 caracteres")
    private String nombreEmpleado;

    @Size(max = 100, message = "El campo 'apellidoEmpleado' no puede exceder 100 caracteres")
    private String apellidoEmpleado;

    @Size(max = 50, message = "El campo 'rolEmpleado' no puede exceder 50 caracteres")
    private String rolEmpleado;

    private Long identificacionProveedor;

    @Size(max = 100, message = "El campo 'nombreProveedor' no puede exceder 100 caracteres")
    private String nombreProveedor;

    // Si viene informado, se agrega el detalle a esa orden especifica en lugar de buscar por cliente
    private String numeroOrden;

    private List<OrdenDetalleRequestDTO> detalles;
}
