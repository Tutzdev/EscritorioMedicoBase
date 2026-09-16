package io.github.officemed.medical_office_api.clinic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "clinic_settings")
public class ClinicSettings {

    public static final UUID DEFAULT_ID = UUID.fromString(
            "00000000-0000-0000-0000-000000000001"
    );

    @Id
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "legal_name", length = 160)
    private String legalName;

    @Column(length = 20)
    private String document;

    @Column(length = 20)
    private String phone;

    @Column(length = 160)
    private String email;

    @Column(length = 300)
    private String address;

    @Column(name = "time_zone", nullable = false, length = 60)
    private String timeZone;

    @Column(name = "primary_color", nullable = false, length = 7)
    private String primaryColor;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ClinicSettings() {
    }

    public void update(
            String displayName,
            String legalName,
            String document,
            String phone,
            String email,
            String address,
            String timeZone,
            String primaryColor,
            String logoUrl
    ) {
        this.displayName = requireText(displayName, "Display name is required").trim();
        this.legalName = normalizeOptionalText(legalName);
        this.document = normalizeOptionalText(document);
        this.phone = normalizeOptionalText(phone);
        this.email = normalizeOptionalEmail(email);
        this.address = normalizeOptionalText(address);
        this.timeZone = requireText(timeZone, "Time zone is required").trim();
        this.primaryColor = requireText(
                primaryColor,
                "Primary color is required"
        ).trim().toUpperCase(Locale.ROOT);
        this.logoUrl = normalizeOptionalText(logoUrl);
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

    public String getDisplayName() {
        return displayName;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getDocument() {
        return document;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
