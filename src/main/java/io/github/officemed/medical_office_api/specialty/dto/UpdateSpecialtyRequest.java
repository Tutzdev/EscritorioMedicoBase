package io.github.officemed.medical_office_api.specialty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSpecialtyRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must contain at most 120 characters")
        String name,

        @Size(max = 500, message = "Description must contain at most 500 characters")
        String description

) {
}
