package dev.ridill.oar_server.common.exception;

import dev.ridill.oar_server.common.logging.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String requestId,
        List<FieldErrorDto> fieldErrors
) {

    public static ErrorResponseDto of(HttpStatus status, String message, HttpServletRequest request) {
        return of(status, message, request, List.of());
    }

    public static ErrorResponseDto of(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<FieldErrorDto> fieldErrors
    ) {
        return new ErrorResponseDto(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                RequestContext.currentRequestId(),
                fieldErrors
        );
    }
}
