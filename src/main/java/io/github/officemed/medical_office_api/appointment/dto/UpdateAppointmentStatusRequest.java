package io.github.officemed.medical_office_api.appointment.dto;

import io.github.officemed.medical_office_api.appointment.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull(message = "Status is required")
        AppointmentStatus status
) {
}
