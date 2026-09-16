package io.github.officemed.medical_office_api.user.exception;

import io.github.officemed.medical_office_api.shared.exception.BusinessRuleException;

public class InvalidUserRoleException extends BusinessRuleException {

    public InvalidUserRoleException(String message) {
        super(message);
    }
}
