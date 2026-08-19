package com.aldo.demo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateClaimRequest(
        @NotNull UUID policyId,
        @NotBlank String incidentType,
        @NotNull @DecimalMin("0.01") BigDecimal estimatedLoss,
        @NotBlank String description) {
}
