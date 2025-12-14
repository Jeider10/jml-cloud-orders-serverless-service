//package com.cloud.jml.utils.connection;
//
//import jakarta.annotation.PostConstruct;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//
//@Getter
//@Setter
//@Slf4j
//@Component
//@ConfigurationProperties(prefix = "products.service")
//public class ProductsServiceProperties {
//
//    private String url;
//
//    @PostConstruct
//    public void validate() {
//        if (url == null || url.isBlank()) {
//            throw new IllegalStateException(
//                    "❌ products.service.url (PRODUCTS_SERVICE_URL) no está configurada"
//            );
//        }
//        log.info("📡 Products Service (PRODUCTS_SERVICE_URL) URL: {}", url);
//    }
//}
