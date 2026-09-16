package io.github.officemed.medical_office_api.specialty.controller;

import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import io.github.officemed.medical_office_api.specialty.dto.CreateSpecialtyRequest;
import io.github.officemed.medical_office_api.specialty.dto.SpecialtyResponse;
import io.github.officemed.medical_office_api.specialty.dto.UpdateSpecialtyRequest;
import io.github.officemed.medical_office_api.specialty.dto.UpdateSpecialtyStatusRequest;
import io.github.officemed.medical_office_api.specialty.service.SpecialtyService;
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
@RequestMapping("/api/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialtyResponse> create(
            @Valid @RequestBody CreateSpecialtyRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(specialtyService.create(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<SpecialtyResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(specialtyService.findAll(search, active, pageable));
    }

    @GetMapping("/{specialtyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SpecialtyResponse> findById(
            @PathVariable UUID specialtyId
    ) {
        return ResponseEntity.ok(specialtyService.findById(specialtyId));
    }

    @PutMapping("/{specialtyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialtyResponse> update(
            @PathVariable UUID specialtyId,
            @Valid @RequestBody UpdateSpecialtyRequest request
    ) {
        return ResponseEntity.ok(specialtyService.update(specialtyId, request));
    }

    @PatchMapping("/{specialtyId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialtyResponse> updateStatus(
            @PathVariable UUID specialtyId,
            @Valid @RequestBody UpdateSpecialtyStatusRequest request
    ) {
        return ResponseEntity.ok(specialtyService.updateStatus(specialtyId, request));
    }
}
