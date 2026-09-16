package io.github.officemed.medical_office_api.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record RescheduleAppointmentRequest(
        @NotNull(message = "Appointment date and time are required")
        @Future(message = "Appointment date and time must be in the future")
        OffsetDateTime scheduledStart
) {
}
