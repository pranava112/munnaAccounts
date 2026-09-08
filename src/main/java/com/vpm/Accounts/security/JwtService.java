package com.vpm.Accounts.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessMinutes;
    public JwtService(@Value("${app.security.jwt-secret}") String secret,
                      @Value("${app.security.access-token-minutes:15}") long accessMinutes) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMinutes = accessMinutes;
    }
    public String accessToken(TenantUserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder().claims(Map.of("tenantId", user.tenantId(), "role", user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")))
            .subject(user.getUsername()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(accessMinutes * 60)))
            .signWith(key).compact();
    }
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
    public boolean isValid(String token) {
        try { return parse(token).getExpiration().after(new Date()); } catch (RuntimeException exception) { return false; }
    }
}
