package io.github.officemed.medical_office_api.professional.controller;

import io.github.officemed.medical_office_api.professional.dto.CreateProfessionalRequest;
import io.github.officemed.medical_office_api.professional.dto.ProfessionalResponse;
import io.github.officemed.medical_office_api.professional.dto.UpdateProfessionalRequest;
import io.github.officemed.medical_office_api.professional.dto.UpdateProfessionalStatusRequest;
import io.github.officemed.medical_office_api.professional.service.ProfessionalService;
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
@RequestMapping("/api/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessionalResponse> create(
            @Valid @RequestBody CreateProfessionalRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(professionalService.create(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ProfessionalResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID specialtyId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(professionalService.findAll(
                search,
                specialtyId,
                active,
                pageable
        ));
    }

    @GetMapping("/{professionalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfessionalResponse> findById(
            @PathVariable UUID professionalId
    ) {
        return ResponseEntity.ok(professionalService.findById(professionalId));
    }

    @PutMapping("/{professionalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessionalResponse> update(
            @PathVariable UUID professionalId,
            @Valid @RequestBody UpdateProfessionalRequest request
    ) {
        return ResponseEntity.ok(professionalService.update(professionalId, request));
    }

    @PatchMapping("/{professionalId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessionalResponse> updateStatus(
            @PathVariable UUID professionalId,
            @Valid @RequestBody UpdateProfessionalStatusRequest request
    ) {
        return ResponseEntity.ok(professionalService.updateStatus(
                professionalId,
                request
        ));
    }
}
