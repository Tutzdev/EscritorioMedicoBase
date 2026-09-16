package io.github.officemed.medical_office_api.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateConsultationRequest(
        @NotBlank(message = "Observations are required")
        @Size(max = 5000, message = "Observations must contain at most 5000 characters")
        String observations
) {
}
