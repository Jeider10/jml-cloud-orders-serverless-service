package com.cloud.jml.config.security;

import com.cloud.jml.config.jwt.JwtAuthenticationEntryPoint;
import com.cloud.jml.config.jwt.JwtRequestFilter;
import com.cloud.jml.utils.general.GeneralUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Slf4j
@Configuration
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final GeneralUtils generalUtils;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, GeneralUtils generalUtils) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.generalUtils = generalUtils;
        log.info("🔥 SecurityConfig inicializado correctamente.");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        String origins = generalUtils.getEnvOrDefault("URL_BASE_MICRO_FRONTEND", "http://localhost:8080");
        List<String> allowedOrigins = List.of(origins.split(","));

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 🔹 Configuracion de CORS: necesaria para que el frontend (por ejemplo localhost:8080)
                // pueda hacer peticiones al backend en otro origen sin ser bloqueadas por el navegador.
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new CorsConfiguration();
                    config.setAllowedOrigins(allowedOrigins); // frontend
//                    config.setAllowedOriginPatterns(List.of("*")); // frontend
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true); // permite enviar headers de autenticacion o cookies
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/roles/**", // Permitir acciones en rol sin autenticacion
                                "/usuario/**", // Permitir acciones en user sin autenticacion
                                "/authentication/**", // Permitir acciones en auth sin autenticacion
                                "/uploads/**" // permite acceso publico a imagenes
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)

                // 🛡️ Cabeceras de seguridad HTTP (actualizadas para Spring Boot 4.x)
                .headers(headers -> headers
                        // Content Security Policy moderna
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'self'; script-src 'self' https://trustedscripts.example.com")
                        )
                        // Evita que el sitio se muestre dentro de iframes (clickjacking)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        // Fuerza el uso de HTTPS y evita ataques de downgrade
                        .httpStrictTransportSecurity(hsts ->
                                hsts.includeSubDomains(true)
                                        .maxAgeInSeconds(31536000)
                        )
                        // FIX: Se cambio .disable() por configuracion activa en contentTypeOptions y cacheControl
                        // .disable() desactivaba las cabeceras de seguridad (comportamiento opuesto al deseado)
                        // Evita deteccion de tipo de contenido (ataques MIME)
                        .contentTypeOptions(cto -> {
                        })
                        // Previene cache no segura
                        .cacheControl(cc -> {
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
