package io.github.officemed.medical_office_api.appointment.entity;

import io.github.officemed.medical_office_api.patient.entity.Patient;
import io.github.officemed.medical_office_api.professional.entity.Professional;
import io.github.officemed.medical_office_api.servicecatalog.entity.ServiceOffering;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceOffering service;

    @Column(name = "scheduled_start", nullable = false)
    private OffsetDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private OffsetDateTime scheduledEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Appointment() {
    }

    private Appointment(
            Patient patient,
            Professional professional,
            ServiceOffering service,
            OffsetDateTime scheduledStart,
            OffsetDateTime scheduledEnd,
            String notes
    ) {
        this.patient = requireValue(patient, "Patient is required");
        this.professional = requireValue(professional, "Professional is required");
        this.service = requireValue(service, "Service is required");
        setSchedule(scheduledStart, scheduledEnd);
        this.notes = normalizeOptionalText(notes);
        this.status = AppointmentStatus.SCHEDULED;
    }

    public static Appointment create(
            Patient patient,
            Professional professional,
            ServiceOffering service,
            OffsetDateTime scheduledStart,
            OffsetDateTime scheduledEnd,
            String notes
    ) {
        return new Appointment(
                patient,
                professional,
                service,
                scheduledStart,
                scheduledEnd,
                notes
        );
    }

    public void reschedule(
            OffsetDateTime scheduledStart,
            OffsetDateTime scheduledEnd
    ) {
        ensureStatusAllows(
                "reschedule",
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED
        );

        setSchedule(scheduledStart, scheduledEnd);
        this.status = AppointmentStatus.SCHEDULED;
    }

    public void confirm() {
        ensureStatusAllows("confirm", AppointmentStatus.SCHEDULED);
        this.status = AppointmentStatus.CONFIRMED;
    }

    public void start() {
        ensureStatusAllows(
                "start",
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED
        );
        this.status = AppointmentStatus.IN_PROGRESS;
    }

    public void complete() {
        ensureStatusAllows("complete", AppointmentStatus.IN_PROGRESS);
        this.status = AppointmentStatus.COMPLETED;
    }

    public void markNoShow() {
        ensureStatusAllows(
                "mark as no-show",
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED
        );
        this.status = AppointmentStatus.NO_SHOW;
    }

    public void cancel(String reason) {
        ensureStatusAllows(
                "cancel",
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED
        );
        this.cancellationReason = requireText(
                reason,
                "Cancellation reason is required"
        ).trim();
        this.status = AppointmentStatus.CANCELED;
    }

    @PrePersist
    private void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = Instant.now();
    }

    private void setSchedule(
            OffsetDateTime scheduledStart,
            OffsetDateTime scheduledEnd
    ) {
        if (scheduledStart == null
                || scheduledEnd == null
                || !scheduledStart.isBefore(scheduledEnd)) {
            throw new IllegalArgumentException(
                    "Appointment start must be before appointment end"
            );
        }

        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
    }

    private void ensureStatusAllows(
            String action,
            AppointmentStatus... allowedStatuses
    ) {
        for (AppointmentStatus allowedStatus : allowedStatuses) {
            if (status == allowedStatus) {
                return;
            }
        }

        throw new IllegalStateException(
                "Cannot " + action + " an appointment with status " + status
        );
    }

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    private <T> T requireValue(
            T value,
            String message
    ) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public UUID getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Professional getProfessional() {
        return professional;
    }

    public ServiceOffering getService() {
        return service;
    }

    public OffsetDateTime getScheduledStart() {
        return scheduledStart;
    }

    public OffsetDateTime getScheduledEnd() {
        return scheduledEnd;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
