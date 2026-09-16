package io.github.officemed.medical_office_api.professional.repository;

import io.github.officemed.medical_office_api.professional.entity.Professional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository
        extends JpaRepository<Professional, UUID>,
        JpaSpecificationExecutor<Professional> {

    boolean existsByRegistrationNumberIgnoreCaseAndRegistrationStateIgnoreCase(
            String registrationNumber,
            String registrationState
    );

    boolean existsByRegistrationNumberIgnoreCaseAndRegistrationStateIgnoreCaseAndIdNot(
            String registrationNumber,
            String registrationState,
            UUID professionalId
    );

    boolean existsByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT professional FROM Professional professional WHERE professional.id = :professionalId")
    Optional<Professional> findByIdForUpdate(@Param("professionalId") UUID professionalId);
}
