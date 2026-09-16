package io.github.officemed.medical_office_api.appointment.exception;

import io.github.officemed.medical_office_api.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class AppointmentNotFoundException extends ResourceNotFoundException {

    public AppointmentNotFoundException(UUID appointmentId) {
        super("Appointment not found: " + appointmentId);
    }
}
