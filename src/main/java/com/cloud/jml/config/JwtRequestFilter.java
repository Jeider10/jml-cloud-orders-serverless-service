package com.cloud.jml.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain) throws ServletException, IOException {

//        String requestURI = request.getRequestURI();
//
//        // ✅ Excluir rutas rutas públicas que no requieren autenticación
//        if (requestURI.startsWith("/auth/") || requestURI.startsWith("/clientes/") || requestURI.startsWith("/proveedores/")
//                || requestURI.startsWith("/user/") || requestURI.startsWith("/subsidiary/") || requestURI.startsWith("/branch/")
//                || requestURI.startsWith("/rol/") || requestURI.startsWith("/user-access/")) {
//            chain.doFilter(request, response);
//            return;
//        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {

                String userName = jwtUtil.extractClaim(token, "userName");
                String role = mapRole(token);

                // Autenticación con rol
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userName, null, List.of(new SimpleGrantedAuthority(role)));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }

    // Mapeo del código numérico a nombre de rol para Spring Security
    private String mapRole(String token) {
        Claims claims = jwtUtil.parseClaims(token);

        Object roleClaim = claims.get("role");
        int role;

        switch (roleClaim) {
            case Integer integer -> role = integer;
            case String string -> role = Integer.parseInt(string);
            default -> throw new IllegalArgumentException("Tipo inesperado para claim 'role': " + roleClaim);
        }

        return switch (role) {
            case 1 -> "ADMIN";
            case 2 -> "USER";
            default -> throw new IllegalArgumentException("Rol desconocido: " + role);
        };
    }
}
