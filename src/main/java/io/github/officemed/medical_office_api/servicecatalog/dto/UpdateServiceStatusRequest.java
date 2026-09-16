package io.github.officemed.medical_office_api.servicecatalog.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateServiceStatusRequest(
        @NotNull(message = "Active status is required")
        Boolean active
) {
}
