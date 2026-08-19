package com.fictitious.insurance.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank String documentNumber,
        @NotBlank String fullName,
        @NotBlank @Email String email) {
}
