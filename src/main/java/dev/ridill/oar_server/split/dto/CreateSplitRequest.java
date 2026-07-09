package dev.ridill.oar_server.split.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateSplitRequest(
        String note,
        @NotNull @Positive BigDecimal totalAmount,
        @NotNull UUID ownerId,
        @NotEmpty List<@Valid SplitParticipantAmount> payments,
        @NotEmpty List<@Valid SplitParticipantAmount> shares
) {
}
