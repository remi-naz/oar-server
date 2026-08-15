package dev.ridill.oar_server.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "google_subject", unique = true)
    private String googleSubject;

    @Column(name = "email", unique = true)
    private String email;

    @Setter
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Setter
    @Column(name = "photo_url")
    private String photoUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private User(String googleSubject, String email, String displayName, String photoUrl) {
        this.googleSubject = googleSubject;
        setEmail(email);
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public static User fromGoogleSignIn(String googleSubject, String email, String displayName, String photoUrl) {
        String resolvedName = (displayName != null && !displayName.isBlank())
                ? displayName
                : localPartOf(email);
        return new User(googleSubject, email, resolvedName, photoUrl);
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public void anonymize() {
        this.deletedAt = Instant.now();
        this.googleSubject = null;
        this.email = null;
        this.photoUrl = null;
        this.displayName = "Deleted user";
    }

    private static String localPartOf(String email) {
        if (email == null || !email.contains("@")) {
            return "User";
        }
        return email.substring(0, email.indexOf('@'));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
