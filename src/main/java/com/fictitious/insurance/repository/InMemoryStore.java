package com.fictitious.insurance.repository;

import com.fictitious.insurance.domain.Claim;
import com.fictitious.insurance.domain.Customer;
import com.fictitious.insurance.domain.Policy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryStore {

    private final Map<UUID, Customer> customers = new ConcurrentHashMap<>();
    private final Map<UUID, Policy> policies = new ConcurrentHashMap<>();
    private final Map<UUID, Claim> claims = new ConcurrentHashMap<>();

    public Map<UUID, Customer> customers() {
        return customers;
    }

    public Map<UUID, Policy> policies() {
        return policies;
    }

    public Map<UUID, Claim> claims() {
        return claims;
    }
}
