package io.github.officemed.medical_office_api.professional.exception;

import io.github.officemed.medical_office_api.shared.exception.ConflictException;

public class UserAlreadyLinkedToProfessionalException extends ConflictException {

    public UserAlreadyLinkedToProfessionalException() {
        super("User is already linked to a professional");
    }
}
