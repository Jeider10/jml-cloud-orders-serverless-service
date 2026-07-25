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
@NoArgsConstructor
@AllArgsConstructor
public class CerrarOrdenRequestDTO {

    @NotNull(message = "La identificacion del cliente es obligatoria")
    private Long identificacionCliente;

    // 🔹 Numero de orden especifico (para cerrar una orden PENDIENTE especifica)
    private String numeroOrden;

    // Valor total recibido (suma de todos los pagos)
    private Long valorRecibido;

    // 🔹 Descuento aplicado
    @Size(max = 20, message = "El tipo de descuento no puede exceder 20 caracteres")
    private String descuentoTipo; // PORCENTAJE o FIJO (puede ser null si no hay descuento)

    private Long descuentoValor; // El % o monto fijo (puede ser null)

    private Long descuentoAplicado; // Monto final descontado en pesos (puede ser null)

    // 🔹 Lista de pagos (pago mixto: efectivo + tarjeta + transferencia, etc.)
    private List<OrdenPagoRequestDTO> pagos;

    // 🔹 Comentario/Observacion (opcional, util para ventas con pago pendiente)
    @Size(max = 500, message = "El comentario no puede exceder 500 caracteres")
    private String comentario;
}
