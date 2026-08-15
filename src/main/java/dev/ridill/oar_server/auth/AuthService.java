package dev.ridill.oar_server.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dev.ridill.oar_server.security.JwtService;
import dev.ridill.oar_server.session.Session;
import dev.ridill.oar_server.session.SessionService;
import dev.ridill.oar_server.user.User;
import dev.ridill.oar_server.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;
    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final TokenHasher tokenHasher;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final UserService userService;
    private final SessionService sessionService;

    @Transactional
    public LoginResponseDto login(String googleIdTokenString, String deviceLabel) {
        GoogleIdToken.Payload payload = googleIdTokenVerifierService.verify(googleIdTokenString)
                .orElseThrow(InvalidGoogleTokenException::new);

        User user = userService.findOrCreateByGoogleSignIn(
                payload.getSubject(),
                payload.getEmail(),
                (String) payload.get("name"),
                (String) payload.get("picture")
        );

        AuthTokensDto tokens = issueTokens(user, deviceLabel);
        return new LoginResponseDto(user.getDisplayName(), user.getPhotoUrl(), tokens.accessToken(), tokens.refreshToken());
    }

    @Transactional
    public AuthTokensDto refresh(String refreshToken) {
        String presentedHash = tokenHasher.hash(refreshToken);

        Optional<Session> match = sessionService.findByRefreshTokenHash(presentedHash);
        if (match.isEmpty()) {
            // Rotated-out token presented again — theft signal. Revoke the whole session.
            sessionService.revokeByPreviousRefreshTokenHash(presentedHash);
            throw new InvalidRefreshTokenException();
        }

        Session session = match.get();
        if (!session.isActive()) throw new InvalidRefreshTokenException();
        userService.findByUserId(session.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        String newRefreshToken = refreshTokenGenerator.generate();
        session.rotate(tokenHasher.hash(newRefreshToken), Instant.now().plus(authProperties.refreshTokenTtl()));
        session.markUsed();

        String newAccessToken = jwtService.generateAccessToken(session.getUserId());
        return new AuthTokensDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        sessionService.revokeByRefreshTokenHash(tokenHasher.hash(refreshToken));
    }

    private AuthTokensDto issueTokens(User user, String deviceLabel) {
        String accessToken = jwtService.generateAccessToken(user.getId());
        String rawRefreshToken = refreshTokenGenerator.generate();
        sessionService.create(
                user.getId(),
                tokenHasher.hash(rawRefreshToken),
                Instant.now().plus(authProperties.refreshTokenTtl()),
                deviceLabel
        );
        return new AuthTokensDto(accessToken, rawRefreshToken);
    }
}
