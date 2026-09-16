package io.github.officemed.medical_office_api.specialty.exception;

import io.github.officemed.medical_office_api.shared.exception.ConflictException;

public class SpecialtyNameAlreadyInUseException extends ConflictException {

    public SpecialtyNameAlreadyInUseException(String name) {
        super("Specialty name is already in use: " + name);
    }
}
