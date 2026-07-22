package io.github.officemed.medical_office_api.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    private User(
            String name,
            String email,
            String passwordHash,
            Role role
    ) {
        this.name = normalizeName(name);
        this.email = normalizeEmail(email);
        this.passwordHash = requireNonBlank(
                passwordHash,
                "Password hash is required"
        );
        this.role = Objects.requireNonNull(role, "Role is required");
        this.active = true;
    }

    public static User create(
            String name,
            String email,
            String passwordHash,
            Role role
    ) {
        return new User(name, email, passwordHash, role);
    }

    public void rename(String name) {
        this.name = normalizeName(name);
    }

    public void changeRole(Role role) {
        this.role = Objects.requireNonNull(role, "Role is required");
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = requireNonBlank(
                passwordHash,
                "Password hash is required"
        );
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    @PrePersist
    private void initializeTimestamps() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void refreshUpdatedAt() {
        updatedAt = Instant.now();
    }

    private static String normalizeName(String name) {
        return requireNonBlank(name, "Name is required").trim();
    }

    private static String normalizeEmail(String email) {
        return requireNonBlank(email, "Email is required")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private static String requireNonBlank(
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

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
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