package io.github.officemed.medical_office_api.clinic.dto;

import java.time.Instant;
import java.util.UUID;

public record ClinicSettingsResponse(
        UUID id,
        String displayName,
        String legalName,
        String document,
        String phone,
        String email,
        String address,
        String timeZone,
        String primaryColor,
        String logoUrl,
        Instant updatedAt
) {
}
