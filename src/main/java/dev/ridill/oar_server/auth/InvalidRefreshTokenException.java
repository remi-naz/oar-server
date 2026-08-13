package dev.ridill.oar_server.auth;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or has already been used");
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
