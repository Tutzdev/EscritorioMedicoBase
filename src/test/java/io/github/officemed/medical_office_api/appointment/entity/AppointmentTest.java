package io.github.officemed.medical_office_api.appointment.entity;

import io.github.officemed.medical_office_api.patient.entity.Patient;
import io.github.officemed.medical_office_api.professional.entity.Professional;
import io.github.officemed.medical_office_api.servicecatalog.entity.ServiceOffering;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AppointmentTest {

    @Test
    void shouldFollowTheExpectedClinicalFlow() {
        Appointment appointment = createAppointment();

        appointment.confirm();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

        appointment.start();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.IN_PROGRESS);

        appointment.complete();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test
    void shouldNotCancelAnAppointmentInProgress() {
        Appointment appointment = createAppointment();
        appointment.start();

        assertThatThrownBy(() -> appointment.cancel("Patient requested"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel");
    }

    @Test
    void shouldReturnToScheduledStatusWhenRescheduled() {
        Appointment appointment = createAppointment();
        appointment.confirm();
        OffsetDateTime newStart = OffsetDateTime.now().plusDays(3);

        appointment.reschedule(newStart, newStart.plusMinutes(45));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointment.getScheduledStart()).isEqualTo(newStart);
    }

    private Appointment createAppointment() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(2);
        return Appointment.create(
                mock(Patient.class),
                mock(Professional.class),
                mock(ServiceOffering.class),
                start,
                start.plusMinutes(30),
                null
        );
    }
}
