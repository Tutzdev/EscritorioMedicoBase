package io.github.officemed.medical_office_api.shared.health;

import java.time.Instant;

public record HealthResponse(
        String status,
        Instant timestamp
) {
}
