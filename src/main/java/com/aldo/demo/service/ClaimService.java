package com.aldo.demo.service;

import com.aldo.demo.api.dto.CreateClaimRequest;
import com.aldo.demo.domain.Claim;
import com.aldo.demo.domain.ClaimStatus;
import com.aldo.demo.domain.Policy;
import com.aldo.demo.repository.InMemoryStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ClaimService {

    private final InMemoryStore store;
    private final PolicyService policyService;

    public ClaimService(InMemoryStore store, PolicyService policyService) {
        this.store = store;
        this.policyService = policyService;
    }

    public Claim create(CreateClaimRequest request) {
        Policy policy = policyService.get(request.policyId());
        LocalDate today = LocalDate.now();
        if (policy.status() != com.aldo.demo.domain.PolicyStatus.ACTIVE
                || today.isBefore(policy.startDate())
                || today.isAfter(policy.endDate())) {
            throw new IllegalArgumentException("Policy is not active on the incident date");
        }
        if (request.estimatedLoss().compareTo(policy.insuredAmount()) > 0) {
            throw new IllegalArgumentException("Estimated loss exceeds insured amount");
        }
        Claim claim = new Claim(
                UUID.randomUUID(),
                policy.id(),
                request.incidentType().toUpperCase(),
                request.estimatedLoss(),
                request.description(),
                ClaimStatus.REGISTERED,
                Instant.now());
        store.claims().put(claim.id(), claim);
        return claim;
    }

    public Claim get(UUID id) {
        Claim claim = store.claims().get(id);
        if (claim == null) {
            throw new IllegalArgumentException("Claim not found: " + id);
        }
        return claim;
    }

    public Claim updateStatus(UUID id, ClaimStatus status) {
        Claim current = get(id);
        validateTransition(current.status(), status);
        Claim updated = new Claim(
                current.id(), current.policyId(), current.incidentType(), current.estimatedLoss(),
                current.description(), status, current.createdAt());
        store.claims().put(id, updated);
        return updated;
    }

    private void validateTransition(ClaimStatus current, ClaimStatus target) {
        boolean valid = switch (current) {
            case REGISTERED -> target == ClaimStatus.UNDER_REVIEW;
            case UNDER_REVIEW -> target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED;
            case APPROVED -> target == ClaimStatus.PAID;
            case REJECTED, PAID -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid claim transition: " + current + " -> " + target);
        }
    }
}
