package io.github.officemed.medical_office_api.availability.exception;

import io.github.officemed.medical_office_api.shared.exception.ConflictException;

public class AvailabilityConflictException extends ConflictException {

    public AvailabilityConflictException() {
        super("Availability overlaps another active interval for this professional");
    }
}
