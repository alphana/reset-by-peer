package com.example.app;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@RestController
public class AbruptBackendController {

    private final Random random = new Random();

    @GetMapping("/simulate-error")
    public Mono<Void> simulateError(ServerHttpRequest request, ServerHttpResponse response) {
        return handleAbruptClosure(request, response);
    }

    @PostMapping("/notifications")
    public Mono<Map<String, Object>> handleNotifications(ServerHttpRequest request, ServerHttpResponse response) {
        if (random.nextBoolean()) {
            return closeAbruptly(request, response);
        } else {
            return sendRandomJsonResponse("notification");
        }
    }

    @PostMapping("/push_messages")
    public Mono<Map<String, Object>> handlePushMessages(ServerHttpRequest request, ServerHttpResponse response) {
        if (random.nextBoolean()) {
            return closeAbruptly(request, response);
        } else {
            return sendRandomJsonResponse("push_message");
        }
    }

    private Mono<Void> handleAbruptClosure(ServerHttpRequest request, ServerHttpResponse response) {
        System.out.println("Closing connection abruptly for request: " + request.getURI());
        response.setComplete().delayElement(Duration.ofSeconds(5)).subscribe(); // Close the response without sending data
        throw new RuntimeException("Connection reset by peer simulated.");
    }

    private Mono<Map<String, Object>> closeAbruptly(ServerHttpRequest request, ServerHttpResponse response) {
        handleAbruptClosure(request, response);
        return Mono.empty(); // Never actually emits a value
    }

    private Mono<Map<String, Object>> sendRandomJsonResponse(String type) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", UUID.randomUUID().toString());
        response.put("type", type);
        response.put("status", random.nextBoolean() ? "success" : "failure");
        response.put("timestamp", System.currentTimeMillis());
        System.out.println("Returning JSON response: " + response);
        return Mono.just(response);
    }
}
