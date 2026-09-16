package io.github.officemed.medical_office_api.servicecatalog.entity;

import io.github.officemed.medical_office_api.specialty.entity.Specialty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "services")
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ServiceOffering() {
    }

    private ServiceOffering(
            Specialty specialty,
            String name,
            String description,
            int durationMinutes,
            BigDecimal price
    ) {
        update(specialty, name, description, durationMinutes, price);
        this.active = true;
    }

    public static ServiceOffering create(
            Specialty specialty,
            String name,
            String description,
            int durationMinutes,
            BigDecimal price
    ) {
        return new ServiceOffering(
                specialty,
                name,
                description,
                durationMinutes,
                price
        );
    }

    public void update(
            Specialty specialty,
            String name,
            String description,
            int durationMinutes,
            BigDecimal price
    ) {
        if (specialty == null) {
            throw new IllegalArgumentException("Specialty is required");
        }

        if (durationMinutes < 5 || durationMinutes > 480) {
            throw new IllegalArgumentException(
                    "Duration must be between 5 and 480 minutes"
            );
        }

        if (price != null && price.signum() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        this.specialty = specialty;
        this.name = requireText(name, "Service name is required").trim();
        this.description = description == null || description.isBlank()
                ? null
                : description.trim();
        this.durationMinutes = durationMinutes;
        this.price = price;
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

    public Specialty getSpecialty() {
        return specialty;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
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
