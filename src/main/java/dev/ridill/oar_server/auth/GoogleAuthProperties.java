package dev.ridill.oar_server.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public record GoogleAuthProperties(String clientId) {
}
