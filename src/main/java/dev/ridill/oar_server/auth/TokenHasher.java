package dev.ridill.oar_server.auth;

import lombok.val;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class TokenHasher {

    public String hash(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            val hash = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("SHA-256 is guaranteed to be available", e);
        }
    }
}
