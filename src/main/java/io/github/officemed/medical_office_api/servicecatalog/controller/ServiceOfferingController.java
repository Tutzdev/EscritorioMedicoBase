package io.github.officemed.medical_office_api.servicecatalog.controller;

import io.github.officemed.medical_office_api.servicecatalog.dto.CreateServiceRequest;
import io.github.officemed.medical_office_api.servicecatalog.dto.ServiceResponse;
import io.github.officemed.medical_office_api.servicecatalog.dto.UpdateServiceRequest;
import io.github.officemed.medical_office_api.servicecatalog.dto.UpdateServiceStatusRequest;
import io.github.officemed.medical_office_api.servicecatalog.service.ServiceOfferingService;
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
@RequestMapping("/api/services")
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    public ServiceOfferingController(ServiceOfferingService serviceOfferingService) {
        this.serviceOfferingService = serviceOfferingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> create(
            @Valid @RequestBody CreateServiceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(serviceOfferingService.create(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ServiceResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID specialtyId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(serviceOfferingService.findAll(
                search,
                specialtyId,
                active,
                pageable
        ));
    }

    @GetMapping("/{serviceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceResponse> findById(
            @PathVariable UUID serviceId
    ) {
        return ResponseEntity.ok(serviceOfferingService.findById(serviceId));
    }

    @PutMapping("/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> update(
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateServiceRequest request
    ) {
        return ResponseEntity.ok(serviceOfferingService.update(serviceId, request));
    }

    @PatchMapping("/{serviceId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> updateStatus(
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateServiceStatusRequest request
    ) {
        return ResponseEntity.ok(serviceOfferingService.updateStatus(serviceId, request));
    }
}
