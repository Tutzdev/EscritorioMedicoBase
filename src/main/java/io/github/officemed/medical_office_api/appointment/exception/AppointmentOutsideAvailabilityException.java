package io.github.officemed.medical_office_api.appointment.exception;

import io.github.officemed.medical_office_api.shared.exception.BusinessRuleException;

public class AppointmentOutsideAvailabilityException extends BusinessRuleException {

    public AppointmentOutsideAvailabilityException() {
        super("Appointment is outside the professional's configured availability");
    }
}
