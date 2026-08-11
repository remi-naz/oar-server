package dev.ridill.oar_server.session;

import dev.ridill.oar_server.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One row per logged-in device. The refresh token itself is never stored — only its
 * hash — and rotation mutates this row in place rather than creating a new one, so a
 * device's session identity is stable across refreshes. Access tokens are stateless
 * JWTs and have no corresponding row here.
 */
@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, unique = true)
    private String refreshTokenHash;

    /**
     * The hash rotate() just replaced. If a request ever presents a hash matching
     * this, that token was already rotated out — a strong signal of reuse/theft —
     * and the session should be revoked.
     */
    @Column(name = "previous_refresh_token_hash", unique = true)
    private String previousRefreshTokenHash;

    @Column(name = "device_label")
    private String deviceLabel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    private Session(User user, String refreshTokenHash, Instant expiresAt, String deviceLabel) {
        this.user = user;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.deviceLabel = deviceLabel;
    }

    public static Session create(User user, String refreshTokenHash, Instant expiresAt, String deviceLabel) {
        return new Session(user, refreshTokenHash, expiresAt, deviceLabel);
    }

    /** Mutates this row in place: today's hash becomes the reuse-detection marker, replaced by the new one. */
    public void rotate(String newRefreshTokenHash, Instant newExpiresAt) {
        this.previousRefreshTokenHash = this.refreshTokenHash;
        this.refreshTokenHash = newRefreshTokenHash;
        this.expiresAt = newExpiresAt;
    }

    public void markUsed() {
        this.lastUsedAt = Instant.now();
    }

    public void revoke() {
        this.revokedAt = Instant.now();
    }

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
