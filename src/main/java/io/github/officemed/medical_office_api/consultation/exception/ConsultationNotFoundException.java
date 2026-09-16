package io.github.officemed.medical_office_api.consultation.exception;

import io.github.officemed.medical_office_api.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class ConsultationNotFoundException extends ResourceNotFoundException {

    public ConsultationNotFoundException(UUID consultationId) {
        super("Consultation not found: " + consultationId);
    }
}
