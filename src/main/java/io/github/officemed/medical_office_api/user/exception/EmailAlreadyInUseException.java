package io.github.officemed.medical_office_api.user.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("Email is already in use: " + email);
    }
}