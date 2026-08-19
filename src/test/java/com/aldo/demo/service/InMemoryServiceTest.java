package com.aldo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InMemoryServiceTest {

    @Test
    void shouldKeepRequestCountAndRecentRequests() {
        InMemoryService service = new InMemoryService();

        service.recordRequest("GET /api/v1/hello");
        service.recordRequest("GET /api/v1/status");

        assertThat(service.requestCount()).isEqualTo(2);
        assertThat(service.recentRequests()).hasSize(2);
        assertThat(service.recentRequests().getFirst()).contains("GET /api/v1/status");
    }
}
