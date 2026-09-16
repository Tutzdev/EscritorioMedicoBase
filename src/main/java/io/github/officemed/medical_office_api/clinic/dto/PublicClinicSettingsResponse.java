package io.github.officemed.medical_office_api.clinic.dto;

public record PublicClinicSettingsResponse(
        String displayName,
        String primaryColor,
        String logoUrl
) {
}
