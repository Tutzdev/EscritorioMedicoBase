package io.github.officemed.medical_office_api.servicecatalog.mapper;

import io.github.officemed.medical_office_api.servicecatalog.dto.ServiceResponse;
import io.github.officemed.medical_office_api.servicecatalog.entity.ServiceOffering;
import org.springframework.stereotype.Component;

@Component
public class ServiceOfferingMapper {

    public ServiceResponse toResponse(ServiceOffering service) {
        return new ServiceResponse(
                service.getId(),
                service.getSpecialty().getId(),
                service.getSpecialty().getName(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getPrice(),
                service.isActive(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }
}
