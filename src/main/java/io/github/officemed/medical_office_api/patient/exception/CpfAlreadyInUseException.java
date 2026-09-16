package io.github.officemed.medical_office_api.patient.exception;

import io.github.officemed.medical_office_api.shared.exception.ConflictException;

public class CpfAlreadyInUseException extends ConflictException {

    public CpfAlreadyInUseException(String cpf) {
        super("CPF is already in use: " + cpf);
    }
}
