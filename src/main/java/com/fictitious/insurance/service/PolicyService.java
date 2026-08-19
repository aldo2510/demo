package com.fictitious.insurance.service;

import com.fictitious.insurance.api.dto.CreatePolicyRequest;
import com.fictitious.insurance.domain.Policy;
import com.fictitious.insurance.domain.PolicyStatus;
import com.fictitious.insurance.repository.InMemoryStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    private final InMemoryStore store;
    private final CustomerService customerService;

    public PolicyService(InMemoryStore store, CustomerService customerService) {
        this.store = store;
        this.customerService = customerService;
    }

    public Policy create(CreatePolicyRequest request) {
        customerService.get(request.customerId());
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Policy end date cannot be before start date");
        }
        BigDecimal premium = calculatePremium(request.insuredAmount(), request.productCode());
        Policy policy = new Policy(
                UUID.randomUUID(),
                request.customerId(),
                request.productCode().toUpperCase(),
                request.vehiclePlate().toUpperCase(),
                request.insuredAmount().setScale(2, RoundingMode.HALF_UP),
                premium,
                request.startDate(),
                request.endDate(),
                PolicyStatus.ACTIVE);
        store.policies().put(policy.id(), policy);
        return policy;
    }

    public Policy get(UUID id) {
        Policy policy = store.policies().get(id);
        if (policy == null) {
            throw new IllegalArgumentException("Policy not found: " + id);
        }
        return policy;
    }

    private BigDecimal calculatePremium(BigDecimal insuredAmount, String productCode) {
        BigDecimal rate = switch (productCode.toUpperCase()) {
            case "AUTO_STANDARD" -> new BigDecimal("0.035");
            case "AUTO_PREMIUM" -> new BigDecimal("0.050");
            default -> new BigDecimal("0.040");
        };
        return insuredAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
