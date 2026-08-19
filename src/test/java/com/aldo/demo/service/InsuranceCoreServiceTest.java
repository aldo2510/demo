package com.aldo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aldo.demo.api.dto.CreateCustomerRequest;
import com.aldo.demo.api.dto.CreatePolicyRequest;
import com.aldo.demo.domain.Customer;
import com.aldo.demo.domain.Policy;
import com.aldo.demo.repository.InMemoryStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class InsuranceCoreServiceTest {

    private final InMemoryStore store = new InMemoryStore();
    private final CustomerService customerService = new CustomerService(store);
    private final PolicyService policyService = new PolicyService(store, customerService);

    @Test
    void shouldCreateCustomer() {
        Customer customer = customerService.create(
                new CreateCustomerRequest("70123456", "Maria Lopez", "maria@example.com"));

        assertThat(customer.id()).isNotNull();
        assertThat(customer.fullName()).isEqualTo("Maria Lopez");
    }

    @Test
    void shouldCalculateStandardAutoPremium() {
        Customer customer = customerService.create(
                new CreateCustomerRequest("70123456", "Maria Lopez", "maria@example.com"));

        Policy policy = policyService.create(new CreatePolicyRequest(
                customer.id(),
                "AUTO_STANDARD",
                "ABC-123",
                new BigDecimal("50000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)));

        assertThat(policy.premium()).isEqualByComparingTo("1750.00");
        assertThat(policy.status().name()).isEqualTo("ACTIVE");
    }
}
