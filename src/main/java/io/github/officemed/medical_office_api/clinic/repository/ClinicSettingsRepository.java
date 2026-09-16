package io.github.officemed.medical_office_api.clinic.repository;

import io.github.officemed.medical_office_api.clinic.entity.ClinicSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClinicSettingsRepository
        extends JpaRepository<ClinicSettings, UUID> {
}
