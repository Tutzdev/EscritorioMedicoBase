package io.github.officemed.medical_office_api.user.dto;

import io.github.officemed.medical_office_api.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Name is required")
        @Size(
                min = 3,
                max = 120,
                message = "Name must contain between 3 and 120 characters"
        )
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(
                max = 160,
                message = "Email must contain at most 160 characters"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 72,
                message = "Password must contain between 8 and 72 characters"
        )
        String password,

        @NotNull(message = "Role is required")
        Role role

) {
}