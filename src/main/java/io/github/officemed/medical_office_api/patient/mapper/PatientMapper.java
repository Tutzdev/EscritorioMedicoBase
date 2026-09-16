package io.github.officemed.medical_office_api.patient.mapper;

import io.github.officemed.medical_office_api.patient.dto.PatientResponse;
import io.github.officemed.medical_office_api.patient.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getCpf(),
                patient.getBirthDate(),
                patient.getPhone(),
                patient.getEmail(),
                patient.isActive(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
}
