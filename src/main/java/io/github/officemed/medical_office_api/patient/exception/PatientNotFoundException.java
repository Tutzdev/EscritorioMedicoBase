package io.github.officemed.medical_office_api.patient.exception;

import io.github.officemed.medical_office_api.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class PatientNotFoundException extends ResourceNotFoundException {

    public PatientNotFoundException(UUID patientId) {
        super("Patient not found: " + patientId);
    }
}
