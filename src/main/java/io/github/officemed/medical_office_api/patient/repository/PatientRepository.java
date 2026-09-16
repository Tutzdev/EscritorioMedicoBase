package io.github.officemed.medical_office_api.patient.repository;

import io.github.officemed.medical_office_api.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PatientRepository
        extends JpaRepository<Patient, UUID>,
        JpaSpecificationExecutor<Patient> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(
            String cpf,
            UUID patientId
    );

    long countByActiveTrue();
}
