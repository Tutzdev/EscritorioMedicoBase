package io.github.officemed.medical_office_api.availability.dto;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record ProfessionalAvailabilityResponse(
        UUID id,
        UUID professionalId,
        String professionalName,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
