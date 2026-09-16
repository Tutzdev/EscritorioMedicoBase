package io.github.officemed.medical_office_api.availability.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateAvailabilityStatusRequest(
        @NotNull(message = "Active status is required")
        Boolean active
) {
}
