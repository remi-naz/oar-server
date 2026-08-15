package dev.ridill.oar_server.auth;

import dev.ridill.oar_server.common.exception.ErrorResponseDto;
import dev.ridill.oar_server.common.exception.ExceptionHandlerOrder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
@Order(ExceptionHandlerOrder.SCOPED)
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidGoogleToken(
            InvalidGoogleTokenException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(ex.getStatus())
                .body(ErrorResponseDto.of(ex.getStatus(), ex.getMessage(), request));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(ex.getStatus())
                .body(ErrorResponseDto.of(ex.getStatus(), ex.getMessage(), request));
    }
}
