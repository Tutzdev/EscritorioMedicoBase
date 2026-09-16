package io.github.officemed.medical_office_api.patient.dto;

import io.github.officemed.medical_office_api.shared.validation.ValidCpf;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePatientRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must contain at most 120 characters")
        String name,

        @NotBlank(message = "CPF is required")
        @ValidCpf
        String cpf,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must contain at most 20 characters")
        String phone,

        @Email(message = "Email must be valid")
        @Size(max = 160, message = "Email must contain at most 160 characters")
        String email

) {
}
