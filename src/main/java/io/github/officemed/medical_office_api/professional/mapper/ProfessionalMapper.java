package io.github.officemed.medical_office_api.professional.mapper;

import io.github.officemed.medical_office_api.professional.dto.ProfessionalResponse;
import io.github.officemed.medical_office_api.professional.entity.Professional;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalMapper {

    public ProfessionalResponse toResponse(Professional professional) {
        return new ProfessionalResponse(
                professional.getId(),
                professional.getUser() == null ? null : professional.getUser().getId(),
                professional.getSpecialty().getId(),
                professional.getSpecialty().getName(),
                professional.getName(),
                professional.getRegistrationNumber(),
                professional.getRegistrationState(),
                professional.getPhone(),
                professional.getEmail(),
                professional.isActive(),
                professional.getCreatedAt(),
                professional.getUpdatedAt()
        );
    }
}
