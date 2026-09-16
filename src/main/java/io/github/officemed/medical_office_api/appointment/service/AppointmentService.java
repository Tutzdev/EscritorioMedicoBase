package io.github.officemed.medical_office_api.appointment.service;

import io.github.officemed.medical_office_api.appointment.dto.AppointmentResponse;
import io.github.officemed.medical_office_api.appointment.dto.CancelAppointmentRequest;
import io.github.officemed.medical_office_api.appointment.dto.CreateAppointmentRequest;
import io.github.officemed.medical_office_api.appointment.dto.RescheduleAppointmentRequest;
import io.github.officemed.medical_office_api.appointment.dto.UpdateAppointmentStatusRequest;
import io.github.officemed.medical_office_api.appointment.entity.Appointment;
import io.github.officemed.medical_office_api.appointment.entity.AppointmentStatus;
import io.github.officemed.medical_office_api.appointment.exception.AppointmentConflictException;
import io.github.officemed.medical_office_api.appointment.exception.AppointmentNotFoundException;
import io.github.officemed.medical_office_api.appointment.exception.AppointmentOutsideAvailabilityException;
import io.github.officemed.medical_office_api.appointment.exception.InvalidAppointmentStatusTransitionException;
import io.github.officemed.medical_office_api.appointment.mapper.AppointmentMapper;
import io.github.officemed.medical_office_api.appointment.repository.AppointmentRepository;
import io.github.officemed.medical_office_api.availability.service.ProfessionalAvailabilityService;
import io.github.officemed.medical_office_api.patient.entity.Patient;
import io.github.officemed.medical_office_api.patient.service.PatientService;
import io.github.officemed.medical_office_api.professional.entity.Professional;
import io.github.officemed.medical_office_api.professional.service.ProfessionalService;
import io.github.officemed.medical_office_api.servicecatalog.entity.ServiceOffering;
import io.github.officemed.medical_office_api.servicecatalog.service.ServiceOfferingService;
import io.github.officemed.medical_office_api.shared.exception.BusinessRuleException;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<AppointmentStatus> BLOCKING_STATUSES = EnumSet.of(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.IN_PROGRESS
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final ProfessionalService professionalService;
    private final ServiceOfferingService serviceOfferingService;
    private final ProfessionalAvailabilityService availabilityService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientService patientService,
            ProfessionalService professionalService,
            ServiceOfferingService serviceOfferingService,
            ProfessionalAvailabilityService availabilityService,
            AppointmentMapper appointmentMapper
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.professionalService = professionalService;
        this.serviceOfferingService = serviceOfferingService;
        this.availabilityService = availabilityService;
        this.appointmentMapper = appointmentMapper;
    }

    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        Patient patient = patientService.findEntityById(request.patientId());
        Professional professional = professionalService.findEntityByIdForUpdate(
                request.professionalId()
        );
        ServiceOffering service = serviceOfferingService.findEntityById(
                request.serviceId()
        );

        validateParticipants(patient, professional, service);

        OffsetDateTime scheduledEnd = calculateEnd(
                request.scheduledStart(),
                service
        );

        validateSchedule(
                professional.getId(),
                null,
                request.scheduledStart(),
                scheduledEnd
        );

        Appointment appointment = Appointment.create(
                patient,
                professional,
                service,
                request.scheduledStart(),
                scheduledEnd,
                request.notes()
        );

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> findAll(
            String search,
            UUID professionalId,
            UUID patientId,
            AppointmentStatus status,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("Start filter must be before end filter");
        }

        Page<AppointmentResponse> appointments = appointmentRepository
                .findAll(
                        buildSpecification(
                                search,
                                professionalId,
                                patientId,
                                status,
                                from,
                                to
                        ),
                        limit(pageable)
                )
                .map(appointmentMapper::toResponse);

        return PageResponse.from(appointments);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID appointmentId) {
        return appointmentMapper.toResponse(findEntityById(appointmentId));
    }

    @Transactional
    public AppointmentResponse reschedule(
            UUID appointmentId,
            RescheduleAppointmentRequest request
    ) {
        Appointment appointment = findEntityById(appointmentId);
        Professional professional = professionalService.findEntityByIdForUpdate(
                appointment.getProfessional().getId()
        );
        OffsetDateTime scheduledEnd = calculateEnd(
                request.scheduledStart(),
                appointment.getService()
        );

        validateSchedule(
                professional.getId(),
                appointmentId,
                request.scheduledStart(),
                scheduledEnd
        );

        try {
            appointment.reschedule(request.scheduledStart(), scheduledEnd);
        } catch (IllegalStateException exception) {
            throw new InvalidAppointmentStatusTransitionException(
                    exception.getMessage()
            );
        }

        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(
            UUID appointmentId,
            CancelAppointmentRequest request
    ) {
        Appointment appointment = findEntityById(appointmentId);

        try {
            appointment.cancel(request.reason());
        } catch (IllegalStateException exception) {
            throw new InvalidAppointmentStatusTransitionException(
                    exception.getMessage()
            );
        }

        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse changeStatus(
            UUID appointmentId,
            UpdateAppointmentStatusRequest request
    ) {
        Appointment appointment = findEntityById(appointmentId);

        try {
            switch (request.status()) {
                case CONFIRMED -> appointment.confirm();
                case IN_PROGRESS -> appointment.start();
                case NO_SHOW -> appointment.markNoShow();
                case COMPLETED -> throw new InvalidAppointmentStatusTransitionException(
                        "Complete the appointment through its consultation record"
                );
                case CANCELED -> throw new InvalidAppointmentStatusTransitionException(
                        "Use the cancellation endpoint and provide a reason"
                );
                case SCHEDULED -> throw new InvalidAppointmentStatusTransitionException(
                        "Use the rescheduling endpoint to return an appointment to scheduled"
                );
            }
        } catch (IllegalStateException exception) {
            throw new InvalidAppointmentStatusTransitionException(
                    exception.getMessage()
            );
        }

        return appointmentMapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Appointment findEntityById(UUID appointmentId) {
        return appointmentRepository
                .findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
    }

    @Transactional
    public Appointment findEntityByIdForUpdate(UUID appointmentId) {
        return appointmentRepository
                .findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
    }

    private void validateParticipants(
            Patient patient,
            Professional professional,
            ServiceOffering service
    ) {
        if (!patient.isActive()) {
            throw new BusinessRuleException("Patient must be active");
        }

        if (!professional.isActive()) {
            throw new BusinessRuleException("Professional must be active");
        }

        if (!service.isActive()) {
            throw new BusinessRuleException("Service must be active");
        }

        if (!professional.getSpecialty().getId().equals(service.getSpecialty().getId())) {
            throw new BusinessRuleException(
                    "Service specialty must match professional specialty"
            );
        }
    }

    private OffsetDateTime calculateEnd(
            OffsetDateTime scheduledStart,
            ServiceOffering service
    ) {
        if (scheduledStart == null || !scheduledStart.isAfter(OffsetDateTime.now())) {
            throw new BusinessRuleException("Appointment must be scheduled in the future");
        }

        return scheduledStart.plusMinutes(service.getDurationMinutes());
    }

    private void validateSchedule(
            UUID professionalId,
            UUID appointmentId,
            OffsetDateTime scheduledStart,
            OffsetDateTime scheduledEnd
    ) {
        if (!scheduledStart.toLocalDate().equals(scheduledEnd.toLocalDate())) {
            throw new AppointmentOutsideAvailabilityException();
        }

        boolean isAvailable = availabilityService.covers(
                professionalId,
                scheduledStart.getDayOfWeek(),
                scheduledStart.toLocalTime(),
                scheduledEnd.toLocalTime()
        );

        if (!isAvailable) {
            throw new AppointmentOutsideAvailabilityException();
        }

        boolean hasConflict = appointmentId == null
                ? appointmentRepository.existsConflict(
                        professionalId,
                        scheduledStart,
                        scheduledEnd,
                        BLOCKING_STATUSES
                )
                : appointmentRepository.existsConflictExcluding(
                        professionalId,
                        appointmentId,
                        scheduledStart,
                        scheduledEnd,
                        BLOCKING_STATUSES
                );

        if (hasConflict) {
            throw new AppointmentConflictException();
        }
    }

    private Specification<Appointment> buildSpecification(
            String search,
            UUID professionalId,
            UUID patientId,
            AppointmentStatus status,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("patient").get("name")),
                                term
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("professional").get("name")),
                                term
                        )
                ));
            }

            if (professionalId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("professional").get("id"),
                        professionalId
                ));
            }

            if (patientId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("patient").get("id"),
                        patientId
                ));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("scheduledStart"),
                        from
                ));
            }

            if (to != null) {
                predicates.add(criteriaBuilder.lessThan(
                        root.get("scheduledStart"),
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
