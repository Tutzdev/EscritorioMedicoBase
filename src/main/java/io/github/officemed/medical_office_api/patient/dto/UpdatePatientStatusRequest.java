package io.github.officemed.medical_office_api.patient.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePatientStatusRequest(

        @NotNull(message = "Active status is required")
        Boolean active

) {
}
