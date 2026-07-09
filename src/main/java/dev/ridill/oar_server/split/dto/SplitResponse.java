package dev.ridill.oar_server.split.dto;

import dev.ridill.oar_server.split.entity.SplitStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SplitResponse(
        UUID id,
        String note,
        BigDecimal totalAmount,
        UUID ownerId,
        SplitStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<Line> payments,
        List<Line> shares
) {
    public record Line(UUID userId, BigDecimal amount) {
    }
}
