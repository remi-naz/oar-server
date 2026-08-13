package dev.ridill.oar_server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and verifies access-token JWTs. Refresh tokens are a separate, opaque
 * mechanism backed by {@link dev.ridill.oar_server.session.Session} — they are
 * never JWTs, since the server needs to be able to revoke/rotate them by hash.
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

    private String generateToken(
            UUID userId,
            Duration ttl,
            String type
    ) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateAccessToken(UUID userId) {
        return generateToken(userId, jwtProperties.accessTokenTtl(), "access_token");
    }

    public String generateRefreshToken(UUID userId) {
        return generateToken(userId, jwtProperties.refreshTokenTtl(), "refresh_token");
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
        val claims = parseClaims(token);
        if (claims == null) return false;

        return claims.get("type").equals("access_token");
    }

    public Boolean isRefreshTokenValid(String token) {
        val claims = parseClaims(token);
        if (claims == null) return false;

        return claims.get("type").equals("refresh_token");
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) throw new IllegalArgumentException("Invalid token");
        return UUID.fromString(claims.getSubject());
    }
}
