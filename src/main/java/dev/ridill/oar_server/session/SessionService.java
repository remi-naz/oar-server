package dev.ridill.oar_server.session;

import dev.ridill.oar_server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public Session create(User user, String refreshTokenHash, Instant expiresAt, String deviceLabel) {
        return sessionRepository.save(Session.create(user, refreshTokenHash, expiresAt, deviceLabel));
    }

    public Optional<Session> findByRefreshTokenHash(String refreshTokenHash) {
        return sessionRepository.findByRefreshTokenHash(refreshTokenHash);
    }

    public Optional<Session> findByPreviousRefreshTokenHash(String previousRefreshTokenHash) {
        return sessionRepository.findByPreviousRefreshTokenHash(previousRefreshTokenHash);
    }

    /** Hard delete. Retained session/refresh-token material for a dead account is pure liability. */
    public long deleteAllForUser(UUID userId) {
        return sessionRepository.deleteByUserId(userId);
    }
}
