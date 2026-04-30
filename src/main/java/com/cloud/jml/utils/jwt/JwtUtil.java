package com.cloud.jml.utils.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private static final String USUARIO = "usuario";
    private final JwtProperties jwtProperties;
    private SecretKey key;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        log.info("🔥 JwtUtil inicializado correctamente.");
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        log.info("🔑 Extrayendo nombre de usuario del token JWT.");

        String usuario = extractClaimValue(token, USUARIO, String.class);
        log.info("✅ Nombre de usuario extraido correctamente: {}", usuario);

        return usuario;
    }

    public Integer extractRoleCode(String token) {
        log.info("🔑 Extrayendo roleCode del token JWT.");

        Integer roleCode = extractClaimValue(token, "roleCode", Integer.class);
        log.info("✅ roleCode extraido correctamente: {}", roleCode);

        return roleCode;
    }

    public String extractRoleName(String token) {
        log.info("🔑 Extrayendo roleName del token JWT.");

        String roleName = extractClaimValue(token, "roleName", String.class);
        log.info("✅ roleName extraido correctamente: {}", roleName);

        return roleName;
    }

    public String extractJti(String token) {
        log.info("🔑 Extrayendo JTI del token JWT.");

        String jti = extractClaimValue(token, "jti", String.class);
        log.info("✅ JTI extraido correctamente: {}", jti);

        return jti;
    }

    public <T> T extractClaimValue(String token, String claimKey, Class<T> type) {
        log.info("🔑 Extrayendo claim '{}' del token JWT.", claimKey);
        try {
            T t = extractAllClaims(token).get(claimKey, type);
            log.info("✅ Claim '{}' extraido correctamente: {}", claimKey, t);
            return t;
        } catch (ExpiredJwtException e) {
            log.warn("⚠️ Token expirado: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("❌ Error al extraer claim '{}': {}", claimKey, e.getMessage());
            throw new JwtException("Token invalido o corrupto", e);
        }
    }

    public Claims extractAllClaims(String token) {
        log.info("🔑 Extrayendo claims del token JWT.");

        Claims claims = Jwts.parser()
                .verifyWith(key) // la clave con la que firmaste el token
                .build()
                .parseSignedClaims(token)
                .getPayload();

        log.info("✅ Claims extraidos correctamente.");

        return claims;
    }

    public void validateToken(String token) {
        try {
            // 🧩 Parsear y validar firma del token
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = claimsJws.getPayload();

            // ⏰ Verificar expiracion manualmente (aunque JJWT ya lo valida)
            if (claims.getExpiration().before(new Date())) {
                String msg = "Token expirado para el usuario: " + claims.get(USUARIO);
                log.warn("⏰ {}", msg);
                throw new ExpiredJwtException(null, claims, msg);
            }

            log.info("✅ Token valido para el usuario: {}", claims.get(USUARIO));

        } catch (ExpiredJwtException e) {
            log.warn("⏰ Token expirado: {}", e.getMessage());
            throw e; // ⚠️ Propaga la excepcion real (sera manejada como 401)
        } catch (JwtException | IllegalArgumentException e) {
            log.error("❌ Token invalido o corrupto: {}", e.getMessage());
            throw new JwtException("Token invalido o corrupto", e);
        }
    }
}
