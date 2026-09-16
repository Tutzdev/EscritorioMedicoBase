package io.github.officemed.medical_office_api.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidRefreshTokenException extends AuthenticationException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or expired");
    }
}
