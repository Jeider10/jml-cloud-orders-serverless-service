package com.cloud.jml.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos
public class OrdenDetalleRequestDTO {

    // FIX: Se agregaron validaciones Jakarta Bean Validation para evitar datos invalidos
    @NotNull(message = "El campo 'codigo' es obligatorio")
    private Long codigo;

    @NotBlank(message = "El campo 'producto' es obligatorio")
    @Size(max = 100, message = "El campo 'producto' no puede exceder 100 caracteres")
    private String producto;

    @Size(max = 200, message = "El campo 'descripcion' no puede exceder 200 caracteres")
    private String descripcion;

    @NotNull(message = "El campo 'cantidad' es obligatorio")
    private Long cantidad;

    @NotNull(message = "El campo 'precio' es obligatorio")
    private Long precio;
}
