package com.fictitious.insurance.service;

import com.fictitious.insurance.api.dto.CreateCustomerRequest;
import com.fictitious.insurance.domain.Customer;
import com.fictitious.insurance.repository.InMemoryStore;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final InMemoryStore store;

    public CustomerService(InMemoryStore store) {
        this.store = store;
    }

    public Customer create(CreateCustomerRequest request) {
        boolean exists = store.customers().values().stream()
                .anyMatch(customer -> customer.documentNumber().equalsIgnoreCase(request.documentNumber()));
        if (exists) {
            throw new IllegalArgumentException("Customer document already exists");
        }
        Customer customer = new Customer(
                UUID.randomUUID(),
                request.documentNumber(),
                request.fullName(),
                request.email(),
                Instant.now());
        store.customers().put(customer.id(), customer);
        return customer;
    }

    public Customer get(UUID id) {
        Customer customer = store.customers().get(id);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + id);
        }
        return customer;
    }
}
