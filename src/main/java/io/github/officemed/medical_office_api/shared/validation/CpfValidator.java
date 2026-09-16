package io.github.officemed.medical_office_api.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context
    ) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String cpf = value.replaceAll("\\D", "");

        if (cpf.length() != 11 || hasAllDigitsEqual(cpf)) {
            return false;
        }

        return calculateDigit(cpf, 9) == Character.getNumericValue(cpf.charAt(9))
                && calculateDigit(cpf, 10) == Character.getNumericValue(cpf.charAt(10));
    }

    private boolean hasAllDigitsEqual(String cpf) {
        return cpf.chars().distinct().count() == 1;
    }

    private int calculateDigit(
            String cpf,
            int digitIndex
    ) {
        int sum = 0;
        int weight = digitIndex + 1;

        for (int index = 0; index < digitIndex; index++) {
            int digit = Character.getNumericValue(cpf.charAt(index));
            sum += digit * (weight - index);
        }

        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
