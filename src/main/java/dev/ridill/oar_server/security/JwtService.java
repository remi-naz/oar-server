package dev.ridill.oar_server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and verifies access-token JWTs. Refresh tokens are opaque secure-random
 * strings tracked by {@link dev.ridill.oar_server.session.Session} — never JWTs,
 * since the server needs to revoke/rotate them by hash rather than by signature.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    @PostConstruct
    private void init() {
        String base64Secret = jwtProperties.secret();
        byte[] decodedSecret = Base64.getDecoder().decode(base64Secret);
        this.secretKey = Keys.hmacShaKeyFor(decodedSecret);
    }

    public String generateAccessToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.accessTokenTtl())))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean isAccessTokenValid(String token) {
        return parseClaims(token) != null;
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) throw new IllegalArgumentException("Invalid token");
        return UUID.fromString(claims.getSubject());
    }
}
