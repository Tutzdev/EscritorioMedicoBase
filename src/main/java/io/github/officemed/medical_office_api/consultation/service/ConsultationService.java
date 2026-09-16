package io.github.officemed.medical_office_api.consultation.service;

import io.github.officemed.medical_office_api.appointment.entity.Appointment;
import io.github.officemed.medical_office_api.appointment.entity.AppointmentStatus;
import io.github.officemed.medical_office_api.appointment.exception.InvalidAppointmentStatusTransitionException;
import io.github.officemed.medical_office_api.appointment.service.AppointmentService;
import io.github.officemed.medical_office_api.consultation.dto.ConsultationResponse;
import io.github.officemed.medical_office_api.consultation.dto.CreateConsultationRequest;
import io.github.officemed.medical_office_api.consultation.dto.UpdateConsultationRequest;
import io.github.officemed.medical_office_api.consultation.entity.Consultation;
import io.github.officemed.medical_office_api.consultation.exception.ConsultationAlreadyExistsException;
import io.github.officemed.medical_office_api.consultation.exception.ConsultationNotFoundException;
import io.github.officemed.medical_office_api.consultation.mapper.ConsultationMapper;
import io.github.officemed.medical_office_api.consultation.repository.ConsultationRepository;
import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ConsultationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ConsultationRepository consultationRepository;
    private final AppointmentService appointmentService;
    private final ConsultationMapper consultationMapper;

    public ConsultationService(
            ConsultationRepository consultationRepository,
            AppointmentService appointmentService,
            ConsultationMapper consultationMapper
    ) {
        this.consultationRepository = consultationRepository;
        this.appointmentService = appointmentService;
        this.consultationMapper = consultationMapper;
    }

    @Transactional
    public ConsultationResponse create(CreateConsultationRequest request) {
        Appointment appointment = appointmentService.findEntityByIdForUpdate(
                request.appointmentId()
        );

        if (consultationRepository.existsByAppointmentId(appointment.getId())) {
            throw new ConsultationAlreadyExistsException();
        }

        startAppointmentIfNecessary(appointment);

        Consultation consultation = Consultation.create(
                appointment,
                request.observations()
        );

        return consultationMapper.toResponse(consultationRepository.save(consultation));
    }

    @Transactional(readOnly = true)
    public PageResponse<ConsultationResponse> findAll(
            String search,
            UUID professionalId,
            UUID patientId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        Page<ConsultationResponse> consultations = consultationRepository
                .findAll(
                        buildSpecification(
                                search,
                                professionalId,
                                patientId,
                                from,
                                to
                        ),
                        limit(pageable)
                )
                .map(consultationMapper::toResponse);

        return PageResponse.from(consultations);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse findById(UUID consultationId) {
        return consultationMapper.toResponse(findEntityById(consultationId));
    }

    @Transactional
    public ConsultationResponse update(
            UUID consultationId,
            UpdateConsultationRequest request
    ) {
        Consultation consultation = findEntityById(consultationId);
        consultation.updateObservations(request.observations());
        return consultationMapper.toResponse(consultation);
    }

    @Transactional
    public ConsultationResponse complete(UUID consultationId) {
        Consultation consultation = findEntityById(consultationId);

        try {
            consultation.getAppointment().complete();
        } catch (IllegalStateException exception) {
            throw new InvalidAppointmentStatusTransitionException(
                    exception.getMessage()
            );
        }

        return consultationMapper.toResponse(consultation);
    }

    private Consultation findEntityById(UUID consultationId) {
        return consultationRepository
                .findById(consultationId)
                .orElseThrow(() -> new ConsultationNotFoundException(consultationId));
    }

    private void startAppointmentIfNecessary(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.IN_PROGRESS) {
            return;
        }

        try {
            appointment.start();
        } catch (IllegalStateException exception) {
            throw new InvalidAppointmentStatusTransitionException(
                    "Consultation cannot be created for an appointment with status "
                            + appointment.getStatus()
            );
        }
    }

    private Specification<Consultation> buildSpecification(
            String search,
            UUID professionalId,
            UUID patientId,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("appointment").get("patient").get("name")),
                                term
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("appointment").get("professional").get("name")),
                                term
                        )
                ));
            }

            if (professionalId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("appointment").get("professional").get("id"),
                        professionalId
                ));
            }

            if (patientId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("appointment").get("patient").get("id"),
                        patientId
                ));
            }

            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("appointment").get("scheduledStart"),
                        from
                ));
            }

            if (to != null) {
                predicates.add(criteriaBuilder.lessThan(
                        root.get("appointment").get("scheduledStart"),
                        to
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Pageable limit(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                pageable.getSort()
        );
    }
}
