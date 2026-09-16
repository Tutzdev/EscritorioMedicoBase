package io.github.officemed.medical_office_api.availability.exception;

import io.github.officemed.medical_office_api.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class ProfessionalAvailabilityNotFoundException extends ResourceNotFoundException {

    public ProfessionalAvailabilityNotFoundException(UUID availabilityId) {
        super("Professional availability not found: " + availabilityId);
    }
}
