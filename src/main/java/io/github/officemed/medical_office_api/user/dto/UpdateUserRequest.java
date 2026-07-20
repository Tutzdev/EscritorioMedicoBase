package io.github.officemed.medical_office_api.user.dto;

import io.github.officemed.medical_office_api.user.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank(message = "Name is required")
        @Size(
                min = 3,
                max = 120,
                message = "Name must contain between 3 and 120 characters"
        )
        String name,

        @NotNull(message = "Role is required")
        Role role

) {
}