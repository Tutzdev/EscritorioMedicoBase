package io.github.officemed.medical_office_api.clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateClinicSettingsRequest(
        @NotBlank(message = "Display name is required")
        @Size(max = 120, message = "Display name must contain at most 120 characters")
        String displayName,

        @Size(max = 160, message = "Legal name must contain at most 160 characters")
        String legalName,

        @Size(max = 20, message = "Document must contain at most 20 characters")
        String document,

        @Size(max = 20, message = "Phone must contain at most 20 characters")
        String phone,

        @Email(message = "Email must be valid")
        @Size(max = 160, message = "Email must contain at most 160 characters")
        String email,

        @Size(max = 300, message = "Address must contain at most 300 characters")
        String address,

        @NotBlank(message = "Time zone is required")
        @Size(max = 60, message = "Time zone must contain at most 60 characters")
        String timeZone,

        @NotBlank(message = "Primary color is required")
        @Pattern(
                regexp = "#[0-9A-Fa-f]{6}",
                message = "Primary color must use the #RRGGBB format"
        )
        String primaryColor,

        @Size(max = 500, message = "Logo URL must contain at most 500 characters")
        String logoUrl
) {
}
