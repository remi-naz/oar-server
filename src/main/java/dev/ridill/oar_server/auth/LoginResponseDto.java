package dev.ridill.oar_server.auth;

public record LoginResponseDto(
        String name,
        String imageUrl,
        String accessToken,
        String refreshToken
) {
}
