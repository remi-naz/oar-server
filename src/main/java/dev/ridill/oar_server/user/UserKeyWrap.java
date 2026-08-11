package dev.ridill.oar_server.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One sealed copy of a user's data encryption key (DEK).
 * <p>
 * {@code wrappedKey} and {@code wrapParams} are opaque to this server: the DEK is
 * generated client-side and sealed under a key the server never sees. Nothing here
 * should ever parse or interpret those two fields.
 */
@Entity
@Table(
        name = "user_key_wraps",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "key_version", "wrap_method"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserKeyWrap {

    private static final int INITIAL_KEY_VERSION = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * All wraps sharing a (user, keyVersion) seal the same DEK.
     */
    @Column(name = "key_version", nullable = false)
    private int keyVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "wrap_method", nullable = false)
    private WrapMethod wrapMethod;

    /**
     * Opaque envelope: nonce &#8214; ciphertext &#8214; auth tag. Never parsed server-side.
     */
    @Column(name = "wrapped_key", nullable = false)
    private byte[] wrappedKey;

    /**
     * KDF algorithm, salt and cost parameters, as client-defined JSON. Stored
     * server-side because a fresh device needs them before it has any local state.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "wrap_params", nullable = false)
    private String wrapParams;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    private UserKeyWrap(User user, int keyVersion, WrapMethod wrapMethod, byte[] wrappedKey, String wrapParams) {
        this.user = user;
        this.keyVersion = keyVersion;
        this.wrapMethod = wrapMethod;
        this.wrappedKey = wrappedKey;
        this.wrapParams = wrapParams;
    }

    public static UserKeyWrap create(User user, WrapMethod wrapMethod, byte[] wrappedKey, String wrapParams) {
        return new UserKeyWrap(user, INITIAL_KEY_VERSION, wrapMethod, wrappedKey, wrapParams);
    }

    /**
     * Rotation writes a fresh set of wraps at a higher version rather than mutating
     * existing rows, so every wrap of a given version stays consistent with its peers.
     */
    public static UserKeyWrap rotate(User user, int keyVersion, WrapMethod wrapMethod, byte[] wrappedKey, String wrapParams) {
        return new UserKeyWrap(user, keyVersion, wrapMethod, wrappedKey, wrapParams);
    }

    public void markUsed() {
        this.lastUsedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserKeyWrap other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
