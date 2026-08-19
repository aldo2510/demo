package com.aldo.demo.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Claim(
        UUID id,
        UUID policyId,
        String incidentType,
        BigDecimal estimatedLoss,
        String description,
        ClaimStatus status,
        Instant createdAt) {
}
