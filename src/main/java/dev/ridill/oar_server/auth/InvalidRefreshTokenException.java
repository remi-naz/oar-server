package dev.ridill.oar_server.auth;

import dev.ridill.oar_server.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException {

    public InvalidRefreshTokenException() {
        this("Refresh token is invalid or has already been used");
    }

    public InvalidRefreshTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
