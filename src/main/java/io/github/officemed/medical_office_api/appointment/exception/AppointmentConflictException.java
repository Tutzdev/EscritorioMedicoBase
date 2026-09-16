package io.github.officemed.medical_office_api.appointment.exception;

import io.github.officemed.medical_office_api.shared.exception.ConflictException;

public class AppointmentConflictException extends ConflictException {

    public AppointmentConflictException() {
        super("Professional already has an appointment during this period");
    }
}
