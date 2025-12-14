//package com.cloud.jml.client;
//
//import com.cloud.jml.utils.general.GeneralUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//@Slf4j
//@Component
//public class ProductosHttpClient {
//
//    private final RestTemplate restTemplate;
//    private final CurrentJwtTokenProvider tokenProvider;
//    private final GeneralUtils generalUtils;
//
//    public ProductosHttpClient(
//            RestTemplate restTemplate,
//            CurrentJwtTokenProvider tokenProvider,
//            GeneralUtils generalUtils
//    ) {
//        this.restTemplate = restTemplate;
//        this.tokenProvider = tokenProvider;
//        this.generalUtils = generalUtils;
//    }
//
//    public void sumarStock(Long codigoProducto, Long cantidad) {
//
//        String baseUrl = generalUtils.getEnvOrDefault(
//                "PRODUCTS_SERVICE_URL",
//                "http://localhost:1084"
//        );
//
//        String url = baseUrl + "/productos/sumar-stock/" + codigoProducto
//                + "?cantidad=" + cantidad;
//
//        String token = tokenProvider.getCurrentToken();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        if (token != null) {
//            headers.setBearerAuth(token);
//        }
//
//        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//
//        log.info("🔄 Enviando solicitud a PRODUCTOS: {}", url);
//
//        restTemplate.exchange(
//                url,
//                HttpMethod.PUT,
//                requestEntity,
//                Void.class
//        );
//    }
//}
