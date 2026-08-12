package dev.ridill.oar_server.auth;

public class InvalidGoogleTokenException extends RuntimeException {

    public InvalidGoogleTokenException() {
        super("Google ID token is missing, expired, or failed verification");
    }
}
