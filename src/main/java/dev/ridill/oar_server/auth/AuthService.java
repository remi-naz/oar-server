package dev.ridill.oar_server.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dev.ridill.oar_server.session.Session;
import dev.ridill.oar_server.session.SessionService;
import dev.ridill.oar_server.user.User;
import dev.ridill.oar_server.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenHasher refreshTokenHasher;
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
        String presentedHash = refreshTokenHasher.hash(refreshToken);

        Session session = sessionService.findByRefreshTokenHash(presentedHash).orElse(null);
        if (session == null) {
            // A hit here means an already-rotated-out token was reused — revoke the session.
            sessionService.findByPreviousRefreshTokenHash(presentedHash).ifPresent(Session::revoke);
            throw new InvalidRefreshTokenException("Refresh token is invalid or has already been used");
        }
        if (!session.isActive()) {
            throw new InvalidRefreshTokenException("Refresh token is expired or revoked");
        }

        String newRawToken = refreshTokenHasher.generateToken();
        session.rotate(refreshTokenHasher.hash(newRawToken), Instant.now().plus(jwtProperties.refreshTokenTtl()));
        session.markUsed();

        String accessToken = jwtService.generateAccessToken(session.getUser().getId());
        return new AuthTokensDto(accessToken, newRawToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        String presentedHash = refreshTokenHasher.hash(refreshToken);
        sessionService.findByRefreshTokenHash(presentedHash)
                .ifPresent(Session::revoke);
    }

    private AuthTokensDto issueTokens(User user, String deviceLabel) {
        String accessToken = jwtService.generateAccessToken(user.getId());
        String rawRefreshToken = refreshTokenHasher.generateToken();
        sessionService.create(
                user,
                refreshTokenHasher.hash(rawRefreshToken),
                Instant.now().plus(jwtProperties.refreshTokenTtl()),
                deviceLabel
        );
        return new AuthTokensDto(accessToken, rawRefreshToken);
    }
}
