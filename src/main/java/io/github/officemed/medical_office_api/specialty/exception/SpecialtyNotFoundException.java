package io.github.officemed.medical_office_api.specialty.exception;

import io.github.officemed.medical_office_api.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class SpecialtyNotFoundException extends ResourceNotFoundException {

    public SpecialtyNotFoundException(UUID specialtyId) {
        super("Specialty not found: " + specialtyId);
    }
}
