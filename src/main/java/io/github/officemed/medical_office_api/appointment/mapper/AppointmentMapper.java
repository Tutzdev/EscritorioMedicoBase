package io.github.officemed.medical_office_api.appointment.mapper;

import io.github.officemed.medical_office_api.appointment.dto.AppointmentResponse;
import io.github.officemed.medical_office_api.appointment.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getProfessional().getId(),
                appointment.getProfessional().getName(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getService().getDurationMinutes(),
                appointment.getScheduledStart(),
                appointment.getScheduledEnd(),
                appointment.getStatus(),
                appointment.getNotes(),
                appointment.getCancellationReason(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
