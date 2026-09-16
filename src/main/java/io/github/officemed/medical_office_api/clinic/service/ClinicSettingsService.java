package io.github.officemed.medical_office_api.clinic.service;

import io.github.officemed.medical_office_api.clinic.dto.ClinicSettingsResponse;
import io.github.officemed.medical_office_api.clinic.dto.PublicClinicSettingsResponse;
import io.github.officemed.medical_office_api.clinic.dto.UpdateClinicSettingsRequest;
import io.github.officemed.medical_office_api.clinic.entity.ClinicSettings;
import io.github.officemed.medical_office_api.clinic.mapper.ClinicSettingsMapper;
import io.github.officemed.medical_office_api.clinic.repository.ClinicSettingsRepository;
import io.github.officemed.medical_office_api.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;

@Service
public class ClinicSettingsService {

    private final ClinicSettingsRepository settingsRepository;
    private final ClinicSettingsMapper settingsMapper;

    public ClinicSettingsService(
            ClinicSettingsRepository settingsRepository,
            ClinicSettingsMapper settingsMapper
    ) {
        this.settingsRepository = settingsRepository;
        this.settingsMapper = settingsMapper;
    }

    @Transactional(readOnly = true)
    public ClinicSettingsResponse find() {
        return settingsMapper.toResponse(findEntity());
    }

    @Transactional(readOnly = true)
    public PublicClinicSettingsResponse findPublic() {
        return settingsMapper.toPublicResponse(findEntity());
    }

    @Transactional
    public ClinicSettingsResponse update(UpdateClinicSettingsRequest request) {
        validateTimeZone(request.timeZone());
        ClinicSettings settings = findEntity();

        settings.update(
                request.displayName(),
                request.legalName(),
                request.document(),
                request.phone(),
                request.email(),
                request.address(),
                request.timeZone(),
                request.primaryColor(),
                request.logoUrl()
        );

        return settingsMapper.toResponse(settings);
    }

    @Transactional(readOnly = true)
    public ClinicSettings findEntity() {
        return settingsRepository
                .findById(ClinicSettings.DEFAULT_ID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Clinic settings were not initialized"
                ));
    }

    private void validateTimeZone(String timeZone) {
        try {
            ZoneId.of(timeZone);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("Time zone is invalid");
        }
    }
}
