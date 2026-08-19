package com.fictitious.insurance.integration;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RiskAssessmentClient {

    private final RestClient restClient;

    public RiskAssessmentClient(
            RestClient.Builder builder,
            @Value("${insurance.integrations.risk.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Map<?, ?> assess(String reference) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/anything/{reference}").build(reference))
                .retrieve()
                .body(Map.class);
    }
}
