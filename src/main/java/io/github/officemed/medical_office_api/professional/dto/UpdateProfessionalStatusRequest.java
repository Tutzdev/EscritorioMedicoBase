package io.github.officemed.medical_office_api.professional.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateProfessionalStatusRequest(
        @NotNull(message = "Active status is required")
        Boolean active
) {
}
