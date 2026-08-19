package com.fictitious.insurance.api;

import com.fictitious.insurance.integration.RiskAssessmentClient;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {

    private final RiskAssessmentClient client;

    public RiskController(RiskAssessmentClient client) {
        this.client = client;
    }

    @GetMapping("/{reference}")
    public Map<?, ?> assess(@PathVariable String reference) {
        return client.assess(reference);
    }
}
