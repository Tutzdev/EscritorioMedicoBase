package io.github.officemed.medical_office_api.servicecatalog.exception;

import io.github.officemed.medical_office_api.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class ServiceNotFoundException extends ResourceNotFoundException {

    public ServiceNotFoundException(UUID serviceId) {
        super("Service not found: " + serviceId);
    }
}
