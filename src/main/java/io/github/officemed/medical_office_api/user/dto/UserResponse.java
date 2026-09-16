package io.github.officemed.medical_office_api.user.dto;

import io.github.officemed.medical_office_api.user.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
