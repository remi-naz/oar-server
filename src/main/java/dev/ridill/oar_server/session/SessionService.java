package dev.ridill.oar_server.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public Optional<Session> findByRefreshTokenHash(String refreshTokenHash) {
        return sessionRepository.findByRefreshTokenHash(refreshTokenHash);
    }

    public void revokeByRefreshTokenHash(String refreshTokenHash) {
        sessionRepository.findByRefreshTokenHash(refreshTokenHash)
                .ifPresent(Session::revoke);
    }

    public Session create(UUID userId, String refreshTokenHash, Instant expiresAt, String deviceLabel) {
        return sessionRepository.save(Session.create(userId, refreshTokenHash, expiresAt, deviceLabel));
    }

    /**
     * Commits independently of the caller's transaction: the caller rejects the
     * request by throwing, and a rollback would silently discard the revocation.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeByPreviousRefreshTokenHash(String previousRefreshTokenHash) {
        sessionRepository.findByPreviousRefreshTokenHash(previousRefreshTokenHash)
                .ifPresent(Session::revoke);
    }

    /**
     * Hard delete. Retained session/refresh-token material for a dead account is pure liability.
     */
    public void deleteAllForUser(UUID userId) {
        sessionRepository.deleteByUserId(userId);
    }
}
