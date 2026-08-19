package com.fictitious.insurance.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePolicyRequest(
        @NotNull UUID customerId,
        @NotBlank String productCode,
        @NotBlank String vehiclePlate,
        @NotNull @DecimalMin("1000.00") BigDecimal insuredAmount,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) {
}
