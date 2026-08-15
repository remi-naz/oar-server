package dev.ridill.oar_server.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(Duration refreshTokenTtl) {
}
