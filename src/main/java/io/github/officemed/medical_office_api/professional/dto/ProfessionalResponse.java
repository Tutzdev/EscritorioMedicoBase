package io.github.officemed.medical_office_api.professional.dto;

import java.time.Instant;
import java.util.UUID;

public record ProfessionalResponse(
        UUID id,
        UUID userId,
        UUID specialtyId,
        String specialtyName,
        String name,
        String registrationNumber,
        String registrationState,
        String phone,
        String email,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
