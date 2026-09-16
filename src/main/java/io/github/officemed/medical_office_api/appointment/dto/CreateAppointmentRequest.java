package io.github.officemed.medical_office_api.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull(message = "Patient is required")
        UUID patientId,

        @NotNull(message = "Professional is required")
        UUID professionalId,

        @NotNull(message = "Service is required")
        UUID serviceId,

        @NotNull(message = "Appointment date and time are required")
        @Future(message = "Appointment date and time must be in the future")
        OffsetDateTime scheduledStart,

        @Size(max = 1000, message = "Notes must contain at most 1000 characters")
        String notes
) {
}
