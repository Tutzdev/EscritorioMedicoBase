package io.github.officemed.medical_office_api.availability.mapper;

import io.github.officemed.medical_office_api.availability.dto.ProfessionalAvailabilityResponse;
import io.github.officemed.medical_office_api.availability.entity.ProfessionalAvailability;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalAvailabilityMapper {

    public ProfessionalAvailabilityResponse toResponse(
            ProfessionalAvailability availability
    ) {
        return new ProfessionalAvailabilityResponse(
                availability.getId(),
                availability.getProfessional().getId(),
                availability.getProfessional().getName(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.isActive(),
                availability.getCreatedAt(),
                availability.getUpdatedAt()
        );
    }
}
