package io.github.officemed.medical_office_api.specialty.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateSpecialtyStatusRequest(

        @NotNull(message = "Active status is required")
        Boolean active

) {
}
