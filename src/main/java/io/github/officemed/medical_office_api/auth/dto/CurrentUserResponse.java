package io.github.officemed.medical_office_api.auth.dto;

import io.github.officemed.medical_office_api.user.entity.Role;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String name,
        String email,
        Role role
) {
}
