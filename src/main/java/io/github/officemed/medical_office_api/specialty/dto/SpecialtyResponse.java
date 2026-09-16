package io.github.officemed.medical_office_api.specialty.dto;

import java.time.Instant;
import java.util.UUID;

public record SpecialtyResponse(
        UUID id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
