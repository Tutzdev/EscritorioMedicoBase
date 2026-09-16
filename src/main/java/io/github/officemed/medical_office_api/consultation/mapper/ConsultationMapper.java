package io.github.officemed.medical_office_api.consultation.mapper;

import io.github.officemed.medical_office_api.appointment.entity.Appointment;
import io.github.officemed.medical_office_api.consultation.dto.ConsultationResponse;
import io.github.officemed.medical_office_api.consultation.entity.Consultation;
import org.springframework.stereotype.Component;

@Component
public class ConsultationMapper {

    public ConsultationResponse toResponse(Consultation consultation) {
        Appointment appointment = consultation.getAppointment();

        return new ConsultationResponse(
                consultation.getId(),
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getProfessional().getId(),
                appointment.getProfessional().getName(),
                appointment.getScheduledStart(),
                appointment.getStatus(),
                consultation.getObservations(),
                consultation.getCreatedAt(),
                consultation.getUpdatedAt()
        );
    }
}
