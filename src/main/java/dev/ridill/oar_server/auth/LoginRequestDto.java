package dev.ridill.oar_server.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(@NotBlank String idToken, String deviceLabel) {
}
