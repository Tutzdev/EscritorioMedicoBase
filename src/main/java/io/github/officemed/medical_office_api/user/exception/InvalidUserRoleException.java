package io.github.officemed.medical_office_api.user.exception;

public class InvalidUserRoleException extends RuntimeException {

    public InvalidUserRoleException(string role) {super("Invalid user role: " + role);
}