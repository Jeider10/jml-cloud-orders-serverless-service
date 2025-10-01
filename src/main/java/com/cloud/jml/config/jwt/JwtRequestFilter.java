package com.cloud.jml.config.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        log.info("🔥 JwtRequestFilter inicializado correctamente.");
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain) throws ServletException, IOException {

//        String requestURI = request.getRequestURI();
//
//        // ✅ Excluir rutas rutas públicas que no requieren autenticación
//        if (requestURI.startsWith("/role/")
//                || requestURI.startsWith("/usuario/")
//                || requestURI.startsWith("/authentication/")) {
//            chain.doFilter(request, response);
//            return;
//        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {

                String userName = jwtUtil.extractClaim(token, "userName");
                String roleCode = mapRole(token);

                // Autenticación con rol
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userName, null, List.of(new SimpleGrantedAuthority(roleCode)));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }

    // Mapeo del código numérico a nombre de rol para Spring Security
    private String mapRole(String token) {
        Integer roleCode = jwtUtil.extractRoleCode(token);
        return switch (roleCode) {
            case 1 -> "ADMIN";
            case 2 -> "USER";
            default -> throw new IllegalArgumentException("Rol desconocido: " + roleCode);
        };
    }

    private String mapRoleCode(String token) {
        Claims claims = jwtUtil.parseClaims(token);

        Object roleClaim = claims.get("roleCode");
        int roleCode;

        switch (roleClaim) {
            case Integer integer -> roleCode = integer;
            case String string -> roleCode = Integer.parseInt(string);
            default -> throw new IllegalArgumentException("Tipo inesperado para claim 'roleCode': " + roleClaim);
        }

        return switch (roleCode) {
            case 1 -> "ADMIN";
            case 2 -> "USER";
            default -> throw new IllegalArgumentException("RoleCode desconocido: " + roleCode);
        };
    }
}
