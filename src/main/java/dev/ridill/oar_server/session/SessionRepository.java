package dev.ridill.oar_server.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    /** A hit here means an already-rotated-out token was presented again — revoke the session. */
    Optional<Session> findByPreviousRefreshTokenHash(String previousRefreshTokenHash);

    List<Session> findByUserIdAndRevokedAtIsNull(UUID userId);

    /**
     * Hard delete. Retained session/refresh-token material for a dead account is pure liability.
     */
    void deleteByUserId(UUID userId);
}
