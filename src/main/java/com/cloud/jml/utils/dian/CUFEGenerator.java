package com.cloud.jml.utils.dian;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class CUFEGenerator {

    // Constructor privado para evitar instanciacion de clase utilitaria
    private CUFEGenerator() {
        throw new UnsupportedOperationException("Clase utilitaria, no se puede instanciar");
    }

    /**
     * Genera el CUFE (Codigo Unico de Facturacion Electronica) usando SHA-384.
     * Concatena los parametros y genera un hash hexadecimal.
     */
    public static String generarCUFE(String nit,
                                     String numeroFactura,
                                     String fecha,
                                     String total,
                                     String iva,
                                     String claveTecnica) {

        log.info("🔐 [CUFE] Iniciando generacion de CUFE para factura: {}", numeroFactura);
        log.debug("🔐 [CUFE] Parametros -> nit={}, fecha={}, total={}, iva={}", nit, fecha, total, iva);

        try {
            // Concatenar todos los datos de entrada para generar el hash
            String data = nit + numeroFactura + fecha + total + iva + claveTecnica;
            log.debug("🔐 [CUFE] Cadena concatenada para hash (longitud={})", data.length());

            // Obtener instancia del algoritmo SHA-384
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            log.debug("🔐 [CUFE] Algoritmo SHA-384 obtenido correctamente");

            // Generar el hash a partir de los bytes de la cadena
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            log.debug("🔐 [CUFE] Hash generado correctamente (bytes={})", hash.length);

            // Convertir bytes a representacion hexadecimal
            String cufe = bytesToHex(hash);
            log.info("🔐 [CUFE] CUFE generado exitosamente para factura: {} (longitud={})", numeroFactura, cufe.length());

            return cufe;

        } catch (NoSuchAlgorithmException e) {
            log.error("❌ [CUFE] Algoritmo SHA-384 no disponible en el sistema: {}", e.getMessage(), e);
            throw new RuntimeException("Error generando CUFE: algoritmo SHA-384 no disponible", e);
        } catch (Exception e) {
            log.error("❌ [CUFE] Error inesperado al generar CUFE para factura {}: {}", numeroFactura, e.getMessage(), e);
            throw new RuntimeException("Error generando CUFE", e);
        }
    }

    /**
     * Convierte un arreglo de bytes a su representacion hexadecimal en String.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();

        for (byte b : bytes) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }

        return hex.toString();
    }
}
