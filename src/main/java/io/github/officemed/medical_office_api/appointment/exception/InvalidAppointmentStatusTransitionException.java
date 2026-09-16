package io.github.officemed.medical_office_api.appointment.exception;

import io.github.officemed.medical_office_api.shared.exception.BusinessRuleException;

public class InvalidAppointmentStatusTransitionException extends BusinessRuleException {

    public InvalidAppointmentStatusTransitionException(String message) {
        super(message);
    }
}
