package io.github.officemed.medical_office_api.servicecatalog.exception;

import io.github.officemed.medical_office_api.shared.exception.ConflictException;

public class ServiceNameAlreadyInUseException extends ConflictException {

    public ServiceNameAlreadyInUseException(String name) {
        super("Service name is already in use for this specialty: " + name);
    }
}
