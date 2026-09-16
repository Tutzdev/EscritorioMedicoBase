package io.github.officemed.medical_office_api.servicecatalog.repository;

import io.github.officemed.medical_office_api.servicecatalog.entity.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ServiceOfferingRepository
        extends JpaRepository<ServiceOffering, UUID>,
        JpaSpecificationExecutor<ServiceOffering> {

    boolean existsBySpecialtyIdAndNameIgnoreCase(
            UUID specialtyId,
            String name
    );

    boolean existsBySpecialtyIdAndNameIgnoreCaseAndIdNot(
            UUID specialtyId,
            String name,
            UUID serviceId
    );
}
