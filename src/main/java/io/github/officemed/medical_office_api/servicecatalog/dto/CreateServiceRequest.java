package io.github.officemed.medical_office_api.servicecatalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateServiceRequest(
        @NotNull(message = "Specialty is required")
        UUID specialtyId,

        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must contain at most 120 characters")
        String name,

        @Size(max = 500, message = "Description must contain at most 500 characters")
        String description,

        @Min(value = 5, message = "Duration must be at least 5 minutes")
        @Max(value = 480, message = "Duration must be at most 480 minutes")
        int durationMinutes,

        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        BigDecimal price
) {
}
