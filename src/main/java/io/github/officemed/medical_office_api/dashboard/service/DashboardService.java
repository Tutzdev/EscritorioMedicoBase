package io.github.officemed.medical_office_api.dashboard.service;

import io.github.officemed.medical_office_api.appointment.dto.AppointmentResponse;
import io.github.officemed.medical_office_api.appointment.entity.AppointmentStatus;
import io.github.officemed.medical_office_api.appointment.mapper.AppointmentMapper;
import io.github.officemed.medical_office_api.appointment.repository.AppointmentRepository;
import io.github.officemed.medical_office_api.clinic.entity.ClinicSettings;
import io.github.officemed.medical_office_api.clinic.service.ClinicSettingsService;
import io.github.officemed.medical_office_api.dashboard.dto.DashboardResponse;
import io.github.officemed.medical_office_api.patient.repository.PatientRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class DashboardService {

    private static final Set<AppointmentStatus> ACTIVE_DAY_STATUSES = EnumSet.of(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.IN_PROGRESS,
            AppointmentStatus.COMPLETED
    );
    private static final Set<AppointmentStatus> PENDING_STATUSES = EnumSet.of(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.IN_PROGRESS
    );

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicSettingsService clinicSettingsService;
    private final AppointmentMapper appointmentMapper;

    public DashboardService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            ClinicSettingsService clinicSettingsService,
            AppointmentMapper appointmentMapper
    ) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.clinicSettingsService = clinicSettingsService;
        this.appointmentMapper = appointmentMapper;
    }

    @Transactional(readOnly = true)
    public DashboardResponse load() {
        ClinicSettings settings = clinicSettingsService.findEntity();
        ZoneId zoneId = ZoneId.of(settings.getTimeZone());
        LocalDate referenceDate = LocalDate.now(zoneId);
        OffsetDateTime startOfDay = referenceDate
                .atStartOfDay(zoneId)
                .toOffsetDateTime();
        OffsetDateTime endOfDay = referenceDate
                .plusDays(1)
                .atStartOfDay(zoneId)
                .toOffsetDateTime();

        long appointmentsToday = appointmentRepository
                .countByScheduledStartGreaterThanEqualAndScheduledStartLessThanAndStatusIn(
                        startOfDay,
                        endOfDay,
                        ACTIVE_DAY_STATUSES
                );
        long pendingToday = appointmentRepository
                .countByScheduledStartGreaterThanEqualAndScheduledStartLessThanAndStatusIn(
                        startOfDay,
                        endOfDay,
                        PENDING_STATUSES
                );
        long completedToday = appointmentRepository
                .countByScheduledStartGreaterThanEqualAndScheduledStartLessThanAndStatus(
                        startOfDay,
                        endOfDay,
                        AppointmentStatus.COMPLETED
                );

        List<AppointmentResponse> nextAppointments = appointmentRepository
                .findUpcoming(
                        OffsetDateTime.now(zoneId),
                        PENDING_STATUSES,
                        PageRequest.of(0, 6)
                )
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();

        return new DashboardResponse(
                referenceDate,
                patientRepository.countByActiveTrue(),
                appointmentsToday,
                pendingToday,
                completedToday,
                nextAppointments
        );
    }
}
