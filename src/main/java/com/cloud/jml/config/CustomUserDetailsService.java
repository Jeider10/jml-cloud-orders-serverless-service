package com.cloud.jml.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class CustomUserDetailsService {

//    private final UserRepository userRepository;
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

//    @Override
//    public UserDetails loadUserByUsername(String userName) {
//        log.info("📌 Buscando usuario: {}", userName);
//
//        // Buscar el usuario
//        Optional<UserEntity> userOpt = userRepository.findByUserName(userName);
//
//        if (userOpt.isEmpty()) {
//            log.warn("⚠️ Usuario no encontrado: {}", userName);
//            throw new UsernameNotFoundException("⚠️ Usuario no encontrado");
//        }
//
//        UserEntity usuario = userOpt.get();
//
////        UserEntity usuario = userRepository.findByUserName(username)
////                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
//
//        return User.builder()
//                .username(usuario.getUserName())
//                .password(usuario.getPassword())
//                .roles(usuario.getRoleName())
//                .build();
//    }
}
