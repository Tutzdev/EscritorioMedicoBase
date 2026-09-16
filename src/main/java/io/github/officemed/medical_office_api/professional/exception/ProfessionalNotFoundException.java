package io.github.officemed.medical_office_api.professional.exception;

import io.github.officemed.medical_office_api.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class ProfessionalNotFoundException extends ResourceNotFoundException {

    public ProfessionalNotFoundException(UUID professionalId) {
        super("Professional not found: " + professionalId);
    }
}
