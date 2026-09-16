package io.github.officemed.medical_office_api.appointment.controller;

import io.github.officemed.medical_office_api.appointment.dto.AppointmentResponse;
import io.github.officemed.medical_office_api.appointment.dto.CancelAppointmentRequest;
import io.github.officemed.medical_office_api.appointment.dto.CreateAppointmentRequest;
import io.github.officemed.medical_office_api.appointment.dto.RescheduleAppointmentRequest;
import io.github.officemed.medical_office_api.appointment.dto.UpdateAppointmentStatusRequest;
import io.github.officemed.medical_office_api.appointment.entity.AppointmentStatus;
import io.github.officemed.medical_office_api.appointment.service.AppointmentService;
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

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> create(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(appointmentService.create(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<AppointmentResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID professionalId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @PageableDefault(size = 20, sort = "scheduledStart") Pageable pageable
    ) {
        return ResponseEntity.ok(appointmentService.findAll(
                search,
                professionalId,
                patientId,
                status,
                from,
                to,
                pageable
        ));
    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentResponse> findById(
            @PathVariable UUID appointmentId
    ) {
        return ResponseEntity.ok(appointmentService.findById(appointmentId));
    }

    @PutMapping("/{appointmentId}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> reschedule(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody RescheduleAppointmentRequest request
    ) {
        return ResponseEntity.ok(appointmentService.reschedule(
                appointmentId,
                request
        ));
    }

    @PostMapping("/{appointmentId}/cancellation")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> cancel(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody CancelAppointmentRequest request
    ) {
        return ResponseEntity.ok(appointmentService.cancel(
                appointmentId,
                request
        ));
    }

    @PatchMapping("/{appointmentId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> changeStatus(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        return ResponseEntity.ok(appointmentService.changeStatus(
                appointmentId,
                request
        ));
    }
}
