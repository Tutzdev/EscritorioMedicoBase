package io.github.officemed.medical_office_api.patient.controller;

import io.github.officemed.medical_office_api.patient.dto.CreatePatientRequest;
import io.github.officemed.medical_office_api.patient.dto.PatientResponse;
import io.github.officemed.medical_office_api.patient.dto.UpdatePatientRequest;
import io.github.officemed.medical_office_api.patient.dto.UpdatePatientStatusRequest;
import io.github.officemed.medical_office_api.patient.service.PatientService;
import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponse> create(
            @Valid @RequestBody CreatePatientRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(patientService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<PageResponse<PatientResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(patientService.findAll(search, active, pageable));
    }

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<PatientResponse> findById(
            @PathVariable UUID patientId
    ) {
        return ResponseEntity.ok(patientService.findById(patientId));
    }

    @PutMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponse> update(
            @PathVariable UUID patientId,
            @Valid @RequestBody UpdatePatientRequest request
    ) {
        return ResponseEntity.ok(patientService.update(patientId, request));
    }

    @PatchMapping("/{patientId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponse> updateStatus(
            @PathVariable UUID patientId,
            @Valid @RequestBody UpdatePatientStatusRequest request
    ) {
        return ResponseEntity.ok(patientService.updateStatus(patientId, request));
    }
}
