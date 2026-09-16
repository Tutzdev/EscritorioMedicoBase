package io.github.officemed.medical_office_api.consultation.dto;

import io.github.officemed.medical_office_api.appointment.entity.AppointmentStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsultationResponse(
        UUID id,
        UUID appointmentId,
        UUID patientId,
        String patientName,
        UUID professionalId,
        String professionalName,
        OffsetDateTime scheduledStart,
        AppointmentStatus appointmentStatus,
        String observations,
        Instant createdAt,
        Instant updatedAt
) {
}
