package dev.ridill.oar_server.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request.idToken(), request.deviceLabel());
    }

    @PostMapping("/refresh")
    public AuthTokensDto refresh(@Valid @RequestBody RefreshRequestDto request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequestDto request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
