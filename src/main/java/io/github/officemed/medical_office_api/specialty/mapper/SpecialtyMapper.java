package io.github.officemed.medical_office_api.specialty.mapper;

import io.github.officemed.medical_office_api.specialty.dto.SpecialtyResponse;
import io.github.officemed.medical_office_api.specialty.entity.Specialty;
import org.springframework.stereotype.Component;

@Component
public class SpecialtyMapper {

    public SpecialtyResponse toResponse(Specialty specialty) {
        return new SpecialtyResponse(
                specialty.getId(),
                specialty.getName(),
                specialty.getDescription(),
                specialty.isActive(),
                specialty.getCreatedAt(),
                specialty.getUpdatedAt()
        );
    }
}
