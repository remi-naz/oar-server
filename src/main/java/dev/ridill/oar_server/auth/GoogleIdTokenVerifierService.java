package dev.ridill.oar_server.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

/**
 * Verifies Google ID tokens (from the Android client's Credential Manager flow)
 * directly against Google's public keys. Firebase Admin SDK is deliberately not
 * used here — see the auth migration plan: step one moves the client off Firebase
 * Auth entirely, so the server should not reintroduce a Firebase dependency.
 */
@Service
@RequiredArgsConstructor
public class GoogleIdTokenVerifierService {

    private final GoogleAuthProperties googleAuthProperties;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    void init() {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleAuthProperties.clientId()))
                .build();
    }

    /**
     * @return the verified payload, or empty if the token is invalid, expired, or fails signature/audience checks.
     */
    public Optional<GoogleIdToken.Payload> verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            return Optional.ofNullable(idToken).map(GoogleIdToken::getPayload);
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
