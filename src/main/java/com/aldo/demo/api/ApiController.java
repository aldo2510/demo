package com.aldo.demo.api;

import com.aldo.demo.service.InMemoryService;
import com.aldo.demo.service.RepositoryClient;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final InMemoryService inMemoryService;
    private final RepositoryClient repositoryClient;

    public ApiController(InMemoryService inMemoryService, RepositoryClient repositoryClient) {
        this.inMemoryService = inMemoryService;
        this.repositoryClient = repositoryClient;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        inMemoryService.recordRequest("GET /api/v1/hello");
        return Map.of(
                "message", "Hola desde Java 25",
                "requestCount", inMemoryService.requestCount());
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        inMemoryService.recordRequest("GET /api/v1/status");
        return Map.of(
                "application", "java25-api-ci",
                "status", "UP",
                "javaVersion", Runtime.version().toString(),
                "requestCount", inMemoryService.requestCount());
    }

    @GetMapping("/memory")
    public Map<String, Object> memory() {
        inMemoryService.recordRequest("GET /api/v1/memory");
        return Map.of(
                "items", inMemoryService.items(),
                "requestCount", inMemoryService.requestCount(),
                "recentRequests", inMemoryService.recentRequests());
    }

    @GetMapping("/requests")
    public List<String> requests() {
        inMemoryService.recordRequest("GET /api/v1/requests");
        return inMemoryService.recentRequests();
    }

    @GetMapping("/github/repos/{owner}/{repo}")
    public ResponseEntity<RepositoryClient.RepositoryResponse> githubRepository(
            @PathVariable String owner,
            @PathVariable String repo) {
        inMemoryService.recordRequest("GET /api/v1/github/repos/%s/%s".formatted(owner, repo));
        return ResponseEntity.ok(repositoryClient.getRepository(owner, repo));
    }
}
