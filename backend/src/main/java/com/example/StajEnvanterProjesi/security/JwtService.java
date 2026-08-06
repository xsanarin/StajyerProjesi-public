package com.example.StajEnvanterProjesi.security;

import com.example.StajEnvanterProjesi.entity.Kullanici;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * ADIM 2 — JWT uret/dogrula (jjwt 0.12.x, HS256).
 * Claims: sub=kullaniciId, rol, sorumluId, stajyerId (Gorev 4 yetki izolasyonu icin).
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(Kullanici k) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        var builder = Jwts.builder()
                .subject(String.valueOf(k.getId()))
                .claim("rol", k.getRol().name())
                .issuedAt(now)
                .expiration(exp);
        if (k.getSorumlu() != null) builder.claim("sorumluId", k.getSorumlu().getId());
        if (k.getStajyer() != null) builder.claim("stajyerId", k.getStajyer().getId());
        return builder.signWith(key).compact();
    }

    // Token gecerli degilse (imza/sure) exception firlatir; filter bunu yakalar.
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
