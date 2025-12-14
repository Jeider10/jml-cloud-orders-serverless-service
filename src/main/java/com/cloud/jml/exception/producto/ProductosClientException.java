//package com.cloud.jml.exception.producto;
//
//import org.springframework.http.HttpStatus;
//
//public class ProductosClientException extends ProductoRuntimeException {
//
//    public ProductosClientException(Long codigoProducto, int statusCode) {
//        super(
//                HttpStatus.resolve(statusCode) != null
//                        ? HttpStatus.resolve(statusCode)
//                        : HttpStatus.INTERNAL_SERVER_ERROR,
//                "❌ Error al sumar stock del producto con código: " + codigoProducto
//        );
//    }
//}
