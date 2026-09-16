package io.github.officemed.medical_office_api.user.exception;

import io.github.officemed.medical_office_api.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }

    public UserNotFoundException(String email) {
        super("User not found: " + email);
    }
}
