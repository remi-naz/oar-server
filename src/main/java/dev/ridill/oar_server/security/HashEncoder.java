package dev.ridill.oar_server.security;

import lombok.val;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class HashEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public String hash(String token) {
        return bcrypt.encode(token);
    }

    public String digest(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256);
            val hash = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("SHA-256 is guaranteed to be available", e);
        }
    }

    public Boolean matches(String rawValue, String hash) {
        return bcrypt.matches(rawValue, hash);
    }
}
