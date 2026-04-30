package com.cloud.jml.config.jpa;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class CustomJpaConfig {

    // ===============================
    // 🔹 Configurar JPA programaticamente
    // ===============================
    @Bean
    @ConfigurationProperties(prefix = "spring.jpa")
    public JpaProperties jpaProperties() {
        JpaProperties properties = new JpaProperties();
        properties.setOpenInView(false);
        log.info("spring.jpa.open-in-view: {}", properties.getOpenInView());
        properties.setGenerateDdl(true); // ddl-auto: update
        log.info("spring.jpa.generate-ddl: {}", properties.isGenerateDdl());
        properties.setShowSql(true); // show-sql: true
        log.info("spring.jpa.show-sql: {}", properties.isShowSql());

        return properties;
    }

    // ===============================
    // 🔹 Deshabilitar OpenEntityManagerInViewInterceptor
    // ===============================
    @Bean
    @ConditionalOnMissingBean(OpenEntityManagerInViewInterceptor.class)
    public WebMvcConfigurer disableOpenInViewInterceptor() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NotNull InterceptorRegistry registry) {
                // No registrar nada
            }
        };
    }
}
