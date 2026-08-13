package dev.ridill.oar_server.user;

import dev.ridill.oar_server.session.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserKeyWrapRepository userKeyWrapRepository;
    private final SessionService sessionService;

    public Optional<User> findByUserId(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Finds the user by their Google {@code sub} claim, or provisions a new account
     * on first sign-in.
     */
    @Transactional
    public User findOrCreateByGoogleSignIn(String googleSubject, String email, String displayName, String photoUrl) {
        return userRepository.findByGoogleSubjectAndDeletedAtIsNull(googleSubject)
                .orElseGet(() -> userRepository.save(User.fromGoogleSignIn(googleSubject, email, displayName, photoUrl)));
    }

    /**
     * Soft-deletes the user and hard-deletes their sealed key material and sessions.
     * <p>
     * The FK's {@code ON DELETE CASCADE} never fires here, because the user row
     * survives soft deletion — so related rows must be removed explicitly. All steps
     * share one transaction so an account can never be left anonymized while its
     * key material or refresh tokens linger.
     */
    @Transactional
    public void anonymizeUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No user with id " + userId));

        userKeyWrapRepository.deleteByUserId(userId);
        sessionService.deleteAllForUser(userId);
        user.anonymize();
    }
}
