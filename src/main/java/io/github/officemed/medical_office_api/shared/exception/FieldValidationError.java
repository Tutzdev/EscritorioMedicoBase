package io.github.officemed.medical_office_api.shared.exception;

public record FieldValidationError(
        String field,
        String message
) {
}
