package io.github.officemed.medical_office_api.patient.service;

import io.github.officemed.medical_office_api.patient.dto.CreatePatientRequest;
import io.github.officemed.medical_office_api.patient.dto.PatientResponse;
import io.github.officemed.medical_office_api.patient.dto.UpdatePatientRequest;
import io.github.officemed.medical_office_api.patient.dto.UpdatePatientStatusRequest;
import io.github.officemed.medical_office_api.patient.entity.Patient;
import io.github.officemed.medical_office_api.patient.exception.CpfAlreadyInUseException;
import io.github.officemed.medical_office_api.patient.exception.PatientNotFoundException;
import io.github.officemed.medical_office_api.patient.mapper.PatientMapper;
import io.github.officemed.medical_office_api.patient.repository.PatientRepository;
import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PatientService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(
            PatientRepository patientRepository,
            PatientMapper patientMapper
    ) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    @Transactional
    public PatientResponse create(CreatePatientRequest request) {
        String cpf = normalizeCpf(request.cpf());
        ensureCpfIsAvailable(cpf, null);

        Patient patient = Patient.create(
                request.name(),
                cpf,
                request.birthDate(),
                request.phone(),
                request.email()
        );

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> findAll(
            String search,
            Boolean active,
            Pageable pageable
    ) {
        Page<PatientResponse> patients = patientRepository
                .findAll(buildSpecification(search, active), limit(pageable))
                .map(patientMapper::toResponse);

        return PageResponse.from(patients);
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(UUID patientId) {
        return patientMapper.toResponse(findEntityById(patientId));
    }

    @Transactional
    public PatientResponse update(
            UUID patientId,
            UpdatePatientRequest request
    ) {
        Patient patient = findEntityById(patientId);
        String cpf = normalizeCpf(request.cpf());

        ensureCpfIsAvailable(cpf, patientId);
        patient.updateContactInformation(
                request.name(),
                cpf,
                request.birthDate(),
                request.phone(),
                request.email()
        );

        return patientMapper.toResponse(patient);
    }

    @Transactional
    public PatientResponse updateStatus(
            UUID patientId,
            UpdatePatientStatusRequest request
    ) {
        Patient patient = findEntityById(patientId);

        if (request.active()) {
            patient.activate();
        } else {
            patient.deactivate();
        }

        return patientMapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public Patient findEntityById(UUID patientId) {
        return patientRepository
                .findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    private void ensureCpfIsAvailable(
            String cpf,
            UUID patientId
    ) {
        boolean inUse = patientId == null
                ? patientRepository.existsByCpf(cpf)
                : patientRepository.existsByCpfAndIdNot(cpf, patientId);

        if (inUse) {
            throw new CpfAlreadyInUseException(cpf);
        }
    }

    private Specification<Patient> buildSpecification(
            String search,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                String digits = search.replaceAll("\\D", "");

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), term),
                        criteriaBuilder.like(root.get("cpf"), "%" + digits + "%")
                ));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Pageable limit(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                pageable.getSort()
        );
    }

    private String normalizeCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }
}
