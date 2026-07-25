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
@NoArgsConstructor
@AllArgsConstructor
public class OrdenPagoRequestDTO {

    // Metodo de pago: EFECTIVO, TARJETA_DEBITO, TARJETA_CREDITO, TRANSFERENCIA, NEQUI
    @NotBlank(message = "El metodo de pago es obligatorio")
    @Size(max = 30, message = "El metodo de pago no puede exceder 30 caracteres")
    private String metodoPago;

    @NotNull(message = "El valor del pago es obligatorio")
    private Long valor;

    // Referencia opcional (numero de aprobacion, transaccion, etc.)
    @Size(max = 100, message = "La referencia no puede exceder 100 caracteres")
    private String referencia;
}
