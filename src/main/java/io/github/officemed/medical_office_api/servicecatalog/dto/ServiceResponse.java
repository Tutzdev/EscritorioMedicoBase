package io.github.officemed.medical_office_api.servicecatalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        UUID specialtyId,
        String specialtyName,
        String name,
        String description,
        int durationMinutes,
        BigDecimal price,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
