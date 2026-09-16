package io.github.officemed.medical_office_api.professional.exception;

import io.github.officemed.medical_office_api.shared.exception.ConflictException;

public class RegistrationAlreadyInUseException extends ConflictException {

    public RegistrationAlreadyInUseException(
            String registrationNumber,
            String registrationState
    ) {
        super("Professional registration is already in use: "
                + registrationNumber + "/" + registrationState);
    }
}
