package io.github.officemed.medical_office_api.patient.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String name,
        String cpf,
        LocalDate birthDate,
        String phone,
        String email,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
