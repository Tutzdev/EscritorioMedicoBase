package io.github.officemed.medical_office_api.appointment.service;

import io.github.officemed.medical_office_api.appointment.dto.AppointmentResponse;
import io.github.officemed.medical_office_api.appointment.dto.CreateAppointmentRequest;
import io.github.officemed.medical_office_api.appointment.entity.Appointment;
import io.github.officemed.medical_office_api.appointment.exception.AppointmentConflictException;
import io.github.officemed.medical_office_api.appointment.exception.AppointmentOutsideAvailabilityException;
import io.github.officemed.medical_office_api.appointment.mapper.AppointmentMapper;
import io.github.officemed.medical_office_api.appointment.repository.AppointmentRepository;
import io.github.officemed.medical_office_api.availability.service.ProfessionalAvailabilityService;
import io.github.officemed.medical_office_api.patient.entity.Patient;
import io.github.officemed.medical_office_api.patient.service.PatientService;
import io.github.officemed.medical_office_api.professional.entity.Professional;
import io.github.officemed.medical_office_api.professional.service.ProfessionalService;
import io.github.officemed.medical_office_api.servicecatalog.entity.ServiceOffering;
import io.github.officemed.medical_office_api.servicecatalog.service.ServiceOfferingService;
import io.github.officemed.medical_office_api.specialty.entity.Specialty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private ProfessionalService professionalService;

    @Mock
    private ServiceOfferingService serviceOfferingService;

    @Mock
    private ProfessionalAvailabilityService availabilityService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private Patient patient;

    @Mock
    private Professional professional;

    @Mock
    private ServiceOffering serviceOffering;

    @Mock
    private Specialty specialty;

    private AppointmentService appointmentService;
    private UUID patientId;
    private UUID professionalId;
    private UUID serviceId;
    private OffsetDateTime scheduledStart;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository,
                patientService,
                professionalService,
                serviceOfferingService,
                availabilityService,
                appointmentMapper
        );

        patientId = UUID.randomUUID();
        professionalId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        scheduledStart = OffsetDateTime.now().plusDays(7).withHour(9).withMinute(0);

        when(patientService.findEntityById(patientId)).thenReturn(patient);
        when(professionalService.findEntityByIdForUpdate(professionalId))
                .thenReturn(professional);
        when(serviceOfferingService.findEntityById(serviceId)).thenReturn(serviceOffering);
        when(patient.isActive()).thenReturn(true);
        when(professional.isActive()).thenReturn(true);
        when(serviceOffering.isActive()).thenReturn(true);
        when(professional.getSpecialty()).thenReturn(specialty);
        when(serviceOffering.getSpecialty()).thenReturn(specialty);
        when(specialty.getId()).thenReturn(UUID.randomUUID());
        when(professional.getId()).thenReturn(professionalId);
        when(serviceOffering.getDurationMinutes()).thenReturn(30);
    }

    @Test
    void shouldCreateAppointmentWhenScheduleIsAvailable() {
        AppointmentResponse expectedResponse = org.mockito.Mockito.mock(
                AppointmentResponse.class
        );
        when(availabilityService.covers(
                professionalId,
                scheduledStart.getDayOfWeek(),
                scheduledStart.toLocalTime(),
                scheduledStart.plusMinutes(30).toLocalTime()
        )).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentMapper.toResponse(any(Appointment.class)))
                .thenReturn(expectedResponse);

        AppointmentResponse response = appointmentService.create(request());

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(
                Appointment.class
        );
        verify(appointmentRepository).save(appointmentCaptor.capture());

        Appointment savedAppointment = appointmentCaptor.getValue();
        assertThat(response).isSameAs(expectedResponse);
        assertThat(savedAppointment.getScheduledStart()).isEqualTo(scheduledStart);
        assertThat(savedAppointment.getScheduledEnd())
                .isEqualTo(scheduledStart.plusMinutes(30));
    }

    @Test
    void shouldRejectAppointmentOutsideProfessionalAvailability() {
        when(availabilityService.covers(
                professionalId,
                scheduledStart.getDayOfWeek(),
                scheduledStart.toLocalTime(),
                scheduledStart.plusMinutes(30).toLocalTime()
        )).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.create(request()))
                .isInstanceOf(AppointmentOutsideAvailabilityException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectConflictingAppointment() {
        when(availabilityService.covers(
                professionalId,
                scheduledStart.getDayOfWeek(),
                scheduledStart.toLocalTime(),
                scheduledStart.plusMinutes(30).toLocalTime()
        )).thenReturn(true);
        when(appointmentRepository.existsConflict(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.create(request()))
                .isInstanceOf(AppointmentConflictException.class);

        verify(appointmentRepository, never()).save(any());
    }

    private CreateAppointmentRequest request() {
        return new CreateAppointmentRequest(
                patientId,
                professionalId,
                serviceId,
                scheduledStart,
                "First appointment"
        );
    }
}
