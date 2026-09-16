package io.github.officemed.medical_office_api.patient.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 160)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Patient() {
    }

    private Patient(
            String name,
            String cpf,
            LocalDate birthDate,
            String phone,
            String email
    ) {
        updateContactInformation(name, cpf, birthDate, phone, email);
        this.active = true;
    }

    public static Patient create(
            String name,
            String cpf,
            LocalDate birthDate,
            String phone,
            String email
    ) {
        return new Patient(name, cpf, birthDate, phone, email);
    }

    public void updateContactInformation(
            String name,
            String cpf,
            LocalDate birthDate,
            String phone,
            String email
    ) {
        this.name = requireText(name, "Name is required").trim();
        this.cpf = requireText(cpf, "CPF is required").replaceAll("\\D", "");
        this.birthDate = requirePastDate(birthDate);
        this.phone = requireText(phone, "Phone is required").trim();
        this.email = normalizeOptionalEmail(email);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
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

    private LocalDate requirePastDate(LocalDate date) {
        if (date == null || !date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date must be in the past");
        }

        return date;
    }

    private String normalizeOptionalEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
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

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
