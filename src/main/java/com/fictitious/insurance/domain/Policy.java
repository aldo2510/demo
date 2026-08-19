package com.fictitious.insurance.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record Policy(
        UUID id,
        UUID customerId,
        String productCode,
        String vehiclePlate,
        BigDecimal insuredAmount,
        BigDecimal premium,
        LocalDate startDate,
        LocalDate endDate,
        PolicyStatus status) {
}
