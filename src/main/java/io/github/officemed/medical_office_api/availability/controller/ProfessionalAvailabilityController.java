package io.github.officemed.medical_office_api.availability.controller;

import io.github.officemed.medical_office_api.availability.dto.CreateProfessionalAvailabilityRequest;
import io.github.officemed.medical_office_api.availability.dto.ProfessionalAvailabilityResponse;
import io.github.officemed.medical_office_api.availability.dto.UpdateAvailabilityStatusRequest;
import io.github.officemed.medical_office_api.availability.dto.UpdateProfessionalAvailabilityRequest;
import io.github.officemed.medical_office_api.availability.service.ProfessionalAvailabilityService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/availabilities")
public class ProfessionalAvailabilityController {

    private final ProfessionalAvailabilityService availabilityService;

    public ProfessionalAvailabilityController(
            ProfessionalAvailabilityService availabilityService
    ) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessionalAvailabilityResponse> create(
            @Valid @RequestBody CreateProfessionalAvailabilityRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(availabilityService.create(request));
    }

    @GetMapping("/professional/{professionalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ProfessionalAvailabilityResponse>> findByProfessional(
            @PathVariable UUID professionalId,
            @PageableDefault(size = 20, sort = "dayOfWeek") Pageable pageable
    ) {
        return ResponseEntity.ok(availabilityService.findByProfessional(
                professionalId,
                pageable
        ));
    }

    @PutMapping("/{availabilityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessionalAvailabilityResponse> update(
            @PathVariable UUID availabilityId,
            @Valid @RequestBody UpdateProfessionalAvailabilityRequest request
    ) {
        return ResponseEntity.ok(availabilityService.update(
                availabilityId,
                request
        ));
    }

    @PatchMapping("/{availabilityId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessionalAvailabilityResponse> updateStatus(
            @PathVariable UUID availabilityId,
            @Valid @RequestBody UpdateAvailabilityStatusRequest request
    ) {
        return ResponseEntity.ok(availabilityService.updateStatus(
                availabilityId,
                request
        ));
    }
}
