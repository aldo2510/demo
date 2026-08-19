package com.fictitious.insurance.api;

import com.fictitious.insurance.api.dto.CreateClaimRequest;
import com.fictitious.insurance.domain.Claim;
import com.fictitious.insurance.domain.ClaimStatus;
import com.fictitious.insurance.service.ClaimService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService service;

    public ClaimController(ClaimService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Claim create(@Valid @RequestBody CreateClaimRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public Claim get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/status")
    public Claim updateStatus(@PathVariable UUID id, @RequestParam ClaimStatus status) {
        return service.updateStatus(id, status);
    }
}
