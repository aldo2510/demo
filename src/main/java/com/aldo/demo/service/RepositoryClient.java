package com.aldo.demo.service;

import java.util.Objects;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RepositoryClient {

    private final RestClient restClient;

    public RepositoryClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    public RepositoryResponse getRepository(String owner, String repo) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}", owner, repo)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(
                            response.getStatusCode(),
                            "GitHub API returned " + response.getStatusCode());
                })
                .body(RepositoryResponse.class);
    }

    public record RepositoryResponse(
            long id,
            String name,
            String fullName,
            String htmlUrl,
            String description,
            boolean isPrivate) {

        public RepositoryResponse {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(fullName, "fullName must not be null");
        }
    }
}
