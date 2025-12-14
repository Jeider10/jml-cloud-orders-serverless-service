//package com.cloud.jml.client;
//
//import com.cloud.jml.exception.producto.ProductosClientException;
//import com.cloud.jml.utils.connection.ProductsServiceProperties;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestClient;
//import org.springframework.web.client.RestClientResponseException;
//
//@Slf4j
//@Component
//public class ProductosClient {
//
//    private final RestClient restClient;
//
//    public ProductosClient(RestClient.Builder builder,
//                           ProductsServiceProperties props) {
//        this.restClient = builder
//                .baseUrl(props.getUrl())
//                .build();
//    }
//
//    public void sumarStock(Long codigoProducto, Long cantidad) {
//        try {
//            restClient.put()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/productos/sumar-stock/{codigo}")
//                            .queryParam("cantidad", cantidad)
//                            .build(codigoProducto))
//                    .retrieve()
//                    .toBodilessEntity();
//
//        } catch (RestClientResponseException ex) {
//
//            var status = ex.getStatusCode();
//
//            log.error(
//                    "❌ Error al sumar stock | producto={} | status={} | response={}",
//                    codigoProducto,
//                    status.value(),
//                    ex.getResponseBodyAsString(),
//                    ex
//            );
//
//            throw new ProductosClientException(
//                    codigoProducto,
//                    status.value()
//            );
//        }
//    }
//}
