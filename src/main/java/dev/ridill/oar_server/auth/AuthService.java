package dev.ridill.oar_server.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dev.ridill.oar_server.security.HashEncoder;
import dev.ridill.oar_server.security.JwtProperties;
import dev.ridill.oar_server.security.JwtService;
import dev.ridill.oar_server.session.Session;
import dev.ridill.oar_server.session.SessionService;
import dev.ridill.oar_server.user.User;
import dev.ridill.oar_server.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final HashEncoder hashEncoder;
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
        // Validate token
        if (!jwtService.isRefreshTokenValid(refreshToken))
            throw new InvalidRefreshTokenException();

        // Validate user from token
        val userId = jwtService.getUserIdFromToken(refreshToken);
        userService.findByUserId(userId)
                .orElseThrow(InvalidRefreshTokenException::new);

        // Get token from db and check if it's been rotated out
        String presentedHash = hashEncoder.hash(refreshToken);
        Session session = sessionService.findByUserIdAndRefreshToken(userId, presentedHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        // A hit here means an already-rotated-out token was reused — revoke the session.
        sessionService.findByPreviousRefreshTokenHash(presentedHash).ifPresent(Session::revoke);
        if (!session.isActive()) throw new InvalidRefreshTokenException();

        // Update new refresh token
        String newRefreshToken = jwtService.generateRefreshToken(session.getUserId());
        session.rotate(hashEncoder.digest(newRefreshToken), Instant.now().plus(jwtProperties.refreshTokenTtl()));
        session.markUsed();

        String newAccessToken = jwtService.generateAccessToken(session.getUserId());
        return new AuthTokensDto(newAccessToken, newRefreshToken);
    }

    private AuthTokensDto issueTokens(User user, String deviceLabel) {
        String accessToken = jwtService.generateAccessToken(user.getId());
        String rawRefreshToken = jwtService.generateRefreshToken(user.getId());
        sessionService.create(
                user.getId(),
                hashEncoder.digest(rawRefreshToken),
                Instant.now().plus(jwtProperties.refreshTokenTtl()),
                deviceLabel
        );
        return new AuthTokensDto(accessToken, rawRefreshToken);
    }
}
