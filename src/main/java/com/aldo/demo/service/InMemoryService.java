package com.aldo.demo.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class InMemoryService {

    private static final int MAX_REQUESTS = 10;

    private final AtomicLong requestCount = new AtomicLong();
    private final ConcurrentLinkedDeque<String> recentRequests = new ConcurrentLinkedDeque<>();
    private final List<String> items = List.of("Java 25", "Spring Boot", "Maven", "GitHub Actions");

    public void recordRequest(String request) {
        requestCount.incrementAndGet();
        recentRequests.addFirst(Instant.now() + " - " + request);
        while (recentRequests.size() > MAX_REQUESTS) {
            recentRequests.pollLast();
        }
    }

    public long requestCount() {
        return requestCount.get();
    }

    public List<String> recentRequests() {
        return new ArrayList<>(recentRequests);
    }

    public List<String> items() {
        return items;
    }
}
