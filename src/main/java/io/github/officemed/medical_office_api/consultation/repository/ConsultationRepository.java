package io.github.officemed.medical_office_api.consultation.repository;

import io.github.officemed.medical_office_api.consultation.entity.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface ConsultationRepository
        extends JpaRepository<Consultation, UUID>,
        JpaSpecificationExecutor<Consultation> {

    boolean existsByAppointmentId(UUID appointmentId);

    Optional<Consultation> findByAppointmentId(UUID appointmentId);

    @Override
    @EntityGraph(attributePaths = {
            "appointment",
            "appointment.patient",
            "appointment.professional"
    })
    Page<Consultation> findAll(
            Specification<Consultation> specification,
            Pageable pageable
    );
}
