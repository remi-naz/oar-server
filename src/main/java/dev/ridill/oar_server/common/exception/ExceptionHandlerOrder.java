package dev.ridill.oar_server.common.exception;

import org.springframework.core.Ordered;

public final class ExceptionHandlerOrder {

    /**
     * Order for feature-scoped {@code @RestControllerAdvice(assignableTypes = ...)} handlers.
     * They only ever compete against {@link GlobalExceptionHandler}, never each other,
     * so every scoped handler can reuse this same value.
     */
    public static final int SCOPED = Ordered.HIGHEST_PRECEDENCE;

    private ExceptionHandlerOrder() {
    }
}
