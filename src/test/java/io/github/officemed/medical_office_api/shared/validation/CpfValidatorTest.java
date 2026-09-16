package io.github.officemed.medical_office_api.shared.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @Test
    void shouldAcceptFormattedCpfWithValidCheckDigits() {
        assertThat(validator.isValid("529.982.247-25", null)).isTrue();
    }

    @Test
    void shouldRejectRepeatedDigitsAndInvalidCheckDigits() {
        assertThat(validator.isValid("111.111.111-11", null)).isFalse();
        assertThat(validator.isValid("529.982.247-24", null)).isFalse();
    }
}
