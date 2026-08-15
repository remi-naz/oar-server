package dev.ridill.oar_server.auth;

import dev.ridill.oar_server.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidGoogleTokenException extends ApiException {

    public InvalidGoogleTokenException() {
        super(HttpStatus.UNAUTHORIZED, "Google ID token is missing, expired, or failed verification");
    }
}
