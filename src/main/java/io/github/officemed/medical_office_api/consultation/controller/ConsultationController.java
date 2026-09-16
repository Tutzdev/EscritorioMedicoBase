package io.github.officemed.medical_office_api.consultation.controller;

import io.github.officemed.medical_office_api.consultation.dto.ConsultationResponse;
import io.github.officemed.medical_office_api.consultation.dto.CreateConsultationRequest;
import io.github.officemed.medical_office_api.consultation.dto.UpdateConsultationRequest;
import io.github.officemed.medical_office_api.consultation.service.ConsultationService;
import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ConsultationResponse> create(
            @Valid @RequestBody CreateConsultationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(consultationService.create(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ConsultationResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID professionalId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(consultationService.findAll(
                search,
                professionalId,
                patientId,
                from,
                to,
                pageable
        ));
    }

    @GetMapping("/{consultationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConsultationResponse> findById(
            @PathVariable UUID consultationId
    ) {
        return ResponseEntity.ok(consultationService.findById(consultationId));
    }

    @PutMapping("/{consultationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ConsultationResponse> update(
            @PathVariable UUID consultationId,
            @Valid @RequestBody UpdateConsultationRequest request
    ) {
        return ResponseEntity.ok(consultationService.update(
                consultationId,
                request
        ));
    }

    @PostMapping("/{consultationId}/completion")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ConsultationResponse> complete(
            @PathVariable UUID consultationId
    ) {
        return ResponseEntity.ok(consultationService.complete(consultationId));
    }
}
