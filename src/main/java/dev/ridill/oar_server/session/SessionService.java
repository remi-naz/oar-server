package dev.ridill.oar_server.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public Optional<Session> findByUserIdAndRefreshToken(UUID userId, String refreshTokenHash) {
        return sessionRepository.findByUserIdAndRefreshTokenHash(userId, refreshTokenHash);
    }

    public Session create(UUID userId, String refreshTokenHash, Instant expiresAt, String deviceLabel) {
        return sessionRepository.save(Session.create(userId, refreshTokenHash, expiresAt, deviceLabel));
    }

    public Optional<Session> findByPreviousRefreshTokenHash(String previousRefreshTokenHash) {
        return sessionRepository.findByPreviousRefreshTokenHash(previousRefreshTokenHash);
    }

    /**
     * Hard delete. Retained session/refresh-token material for a dead account is pure liability.
     */
    public void deleteAllForUser(UUID userId) {
        sessionRepository.deleteByUserId(userId);
    }
}
