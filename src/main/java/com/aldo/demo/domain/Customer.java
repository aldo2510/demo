package com.aldo.demo.domain;

import java.time.Instant;
import java.util.UUID;

public record Customer(
        UUID id,
        String documentNumber,
        String fullName,
        String email,
        Instant createdAt) {
}
