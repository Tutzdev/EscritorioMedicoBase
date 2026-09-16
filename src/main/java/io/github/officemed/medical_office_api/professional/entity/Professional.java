package io.github.officemed.medical_office_api.professional.entity;

import io.github.officemed.medical_office_api.specialty.entity.Specialty;
import io.github.officemed.medical_office_api.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "professionals")
public class Professional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "registration_number", nullable = false, length = 40)
    private String registrationNumber;

    @Column(name = "registration_state", nullable = false, length = 2)
    private String registrationState;

    @Column(length = 20)
    private String phone;

    @Column(length = 160)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Professional() {
    }

    private Professional(
            User user,
            Specialty specialty,
            String name,
            String registrationNumber,
            String registrationState,
            String phone,
            String email
    ) {
        this.user = user;
        update(
                specialty,
                name,
                registrationNumber,
                registrationState,
                phone,
                email
        );
        this.active = true;
    }

    public static Professional create(
            User user,
            Specialty specialty,
            String name,
            String registrationNumber,
            String registrationState,
            String phone,
            String email
    ) {
        return new Professional(
                user,
                specialty,
                name,
                registrationNumber,
                registrationState,
                phone,
                email
        );
    }

    public void update(
            Specialty specialty,
            String name,
            String registrationNumber,
            String registrationState,
            String phone,
            String email
    ) {
        if (specialty == null) {
            throw new IllegalArgumentException("Specialty is required");
        }

        this.specialty = specialty;
        this.name = requireText(name, "Name is required").trim();
        this.registrationNumber = requireText(
                registrationNumber,
                "Registration number is required"
        ).trim().toUpperCase(Locale.ROOT);
        this.registrationState = requireText(
                registrationState,
                "Registration state is required"
        ).trim().toUpperCase(Locale.ROOT);
        this.phone = normalizeOptionalText(phone);
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

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeOptionalEmail(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toLowerCase(Locale.ROOT);
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

    public User getUser() {
        return user;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public String getName() {
        return name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getRegistrationState() {
        return registrationState;
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
