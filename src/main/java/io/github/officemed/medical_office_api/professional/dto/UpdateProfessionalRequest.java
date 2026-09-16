package io.github.officemed.medical_office_api.professional.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateProfessionalRequest(
        @NotNull(message = "Specialty is required")
        UUID specialtyId,

        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must contain at most 120 characters")
        String name,

        @NotBlank(message = "Registration number is required")
        @Size(max = 40, message = "Registration number must contain at most 40 characters")
        String registrationNumber,

        @NotBlank(message = "Registration state is required")
        @Pattern(regexp = "[A-Za-z]{2}", message = "Registration state must contain two letters")
        String registrationState,

        @Size(max = 20, message = "Phone must contain at most 20 characters")
        String phone,

        @Email(message = "Email must be valid")
        @Size(max = 160, message = "Email must contain at most 160 characters")
        String email
) {
}
