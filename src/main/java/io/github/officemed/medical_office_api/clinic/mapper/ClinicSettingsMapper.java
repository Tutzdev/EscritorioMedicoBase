package io.github.officemed.medical_office_api.clinic.mapper;

import io.github.officemed.medical_office_api.clinic.dto.ClinicSettingsResponse;
import io.github.officemed.medical_office_api.clinic.dto.PublicClinicSettingsResponse;
import io.github.officemed.medical_office_api.clinic.entity.ClinicSettings;
import org.springframework.stereotype.Component;

@Component
public class ClinicSettingsMapper {

    public ClinicSettingsResponse toResponse(ClinicSettings settings) {
        return new ClinicSettingsResponse(
                settings.getId(),
                settings.getDisplayName(),
                settings.getLegalName(),
                settings.getDocument(),
                settings.getPhone(),
                settings.getEmail(),
                settings.getAddress(),
                settings.getTimeZone(),
                settings.getPrimaryColor(),
                settings.getLogoUrl(),
                settings.getUpdatedAt()
        );
    }

    public PublicClinicSettingsResponse toPublicResponse(ClinicSettings settings) {
        return new PublicClinicSettingsResponse(
                settings.getDisplayName(),
                settings.getPrimaryColor(),
                settings.getLogoUrl()
        );
    }
}
