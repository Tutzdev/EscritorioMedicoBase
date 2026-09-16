package io.github.officemed.medical_office_api.clinic.controller;

import io.github.officemed.medical_office_api.clinic.dto.ClinicSettingsResponse;
import io.github.officemed.medical_office_api.clinic.dto.PublicClinicSettingsResponse;
import io.github.officemed.medical_office_api.clinic.dto.UpdateClinicSettingsRequest;
import io.github.officemed.medical_office_api.clinic.service.ClinicSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinic-settings")
public class ClinicSettingsController {

    private final ClinicSettingsService settingsService;

    public ClinicSettingsController(ClinicSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/public")
    public ResponseEntity<PublicClinicSettingsResponse> findPublic() {
        return ResponseEntity.ok(settingsService.findPublic());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClinicSettingsResponse> find() {
        return ResponseEntity.ok(settingsService.find());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClinicSettingsResponse> update(
            @Valid @RequestBody UpdateClinicSettingsRequest request
    ) {
        return ResponseEntity.ok(settingsService.update(request));
    }
}
