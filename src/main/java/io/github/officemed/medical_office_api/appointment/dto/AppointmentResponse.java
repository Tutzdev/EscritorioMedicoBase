package io.github.officemed.medical_office_api.appointment.dto;

import io.github.officemed.medical_office_api.appointment.entity.AppointmentStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        String patientName,
        UUID professionalId,
        String professionalName,
        UUID serviceId,
        String serviceName,
        int durationMinutes,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        AppointmentStatus status,
        String notes,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt
) {
}
