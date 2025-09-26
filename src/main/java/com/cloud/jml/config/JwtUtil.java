package com.cloud.jml.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ Validar si el token es correcto (firma y expiración)
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return !isTokenExpired(claimsJws.getPayload());
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Extraer un claim específico por clave
    public String extractClaim(String token, String claimKey) {
        Claims claims = parseClaims(token);
        return claims.get(claimKey, String.class);
    }

    // ✅ Verificar expiración
    public boolean isTokenExpired(String token) {
        Claims claims = parseClaims(token);
        return isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    // ✅ Parsear claims de un token
    public Claims parseClaims(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(key)   // Usa la clave HMAC
                .build()
                .parseSignedClaims(token);

        return claimsJws.getPayload();
    }
}
