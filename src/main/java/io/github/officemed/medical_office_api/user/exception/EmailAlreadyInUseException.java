package io.github.officemed.medical_office_api.user.exception;

import io.github.officemed.medical_office_api.shared.exception.ConflictException;

public class EmailAlreadyInUseException extends ConflictException {

    public EmailAlreadyInUseException(String email) {
        super("Email is already in use: " + email);
    }
}
