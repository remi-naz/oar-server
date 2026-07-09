package dev.ridill.oar_server.split.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitParticipantAmount(
        @NotNull UUID userId,
        @NotNull @Positive BigDecimal amount
) {
}
