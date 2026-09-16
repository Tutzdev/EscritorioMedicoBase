package io.github.officemed.medical_office_api.specialty.repository;

import io.github.officemed.medical_office_api.specialty.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SpecialtyRepository
        extends JpaRepository<Specialty, UUID>,
        JpaSpecificationExecutor<Specialty> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID specialtyId
    );
}
