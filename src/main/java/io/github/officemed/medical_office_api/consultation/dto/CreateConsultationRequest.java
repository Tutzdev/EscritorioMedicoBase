package io.github.officemed.medical_office_api.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateConsultationRequest(
        @NotNull(message = "Appointment is required")
        UUID appointmentId,

        @NotBlank(message = "Observations are required")
        @Size(max = 5000, message = "Observations must contain at most 5000 characters")
        String observations
) {
}
