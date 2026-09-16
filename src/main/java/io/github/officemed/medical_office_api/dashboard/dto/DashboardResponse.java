package io.github.officemed.medical_office_api.dashboard.dto;

import io.github.officemed.medical_office_api.appointment.dto.AppointmentResponse;

import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        LocalDate referenceDate,
        long activePatients,
        long appointmentsToday,
        long pendingToday,
        long completedToday,
        List<AppointmentResponse> nextAppointments
) {
}
