package com.cloud.jml.config.customuser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class CustomUserDetailsService {

    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        log.info("🔥 CustomUserDetailsService inicializado correctamente.");
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Usuario dummy para pruebas
        UserDetailsService userDetailsService = username -> User.builder()
                .username("dummy")
                .password(passwordEncoder.encode("dummy"))
                .roles("USER")
                .build();

        log.info("📌 Usuario dummy creado: {}", userDetailsService.loadUserByUsername("dummy"));

        return userDetailsService;
    }
}
