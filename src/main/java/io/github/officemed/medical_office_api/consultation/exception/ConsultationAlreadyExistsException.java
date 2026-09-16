package io.github.officemed.medical_office_api.consultation.exception;

import io.github.officemed.medical_office_api.shared.exception.ConflictException;

public class ConsultationAlreadyExistsException extends ConflictException {

    public ConsultationAlreadyExistsException() {
        super("A consultation is already registered for this appointment");
    }
}
