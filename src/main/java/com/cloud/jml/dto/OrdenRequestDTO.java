package com.cloud.jml.dto;

import jakarta.validation.constraints.NotNull;
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

    // FIX: Se agregaron validaciones Jakarta Bean Validation para evitar datos invalidos
    @NotNull(message = "La identificacion del cliente es obligatoria")
    private Long identificacionCliente;

    //    @NotBlank(message = "El campo 'nombreCliente' es obligatorio")
    @Size(max = 100, message = "El campo 'nombreCliente' no puede exceder 100 caracteres")
    private String nombreCliente;

    //    @NotBlank(message = "El campo 'apellidoCliente' es obligatorio")
    @Size(max = 100, message = "El campo 'apellidoCliente' no puede exceder 100 caracteres")
    private String apellidoCliente;

    private Long identificacionEmpleado;

    @Size(max = 100, message = "El campo 'nombreEmpleado' no puede exceder 100 caracteres")
    private String nombreEmpleado;

    @Size(max = 100, message = "El campo 'apellidoEmpleado' no puede exceder 100 caracteres")
    private String apellidoEmpleado;

    private Long identificacionProveedor;

    @Size(max = 100, message = "El campo 'nombreProveedor' no puede exceder 100 caracteres")
    private String nombreProveedor;

    private List<OrdenDetalleRequestDTO> detalles;
}
