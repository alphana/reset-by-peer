package com.example.demogateway.fix;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Profile("fixed")
@Slf4j
public class ConnectionManager {
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger totalRequests = new AtomicInteger(0);

    @Scheduled(fixedRate = 30000) // Log stats every 30 seconds
    public void logStats() {
        log.info("Connection Stats - Active: {}, Total Requests: {}",
                activeConnections.get(), totalRequests.get());
    }

    public void incrementConnections() {
        activeConnections.incrementAndGet();
        totalRequests.incrementAndGet();
    }

    public void decrementConnections() {
        activeConnections.decrementAndGet();
    }

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    class ConnectionTrackingFilter implements GlobalFilter {
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            incrementConnections();

            exchange.getResponse().beforeCommit(() -> {
                exchange.getResponse().getHeaders().add("X-Connection-ID",
                        String.valueOf(totalRequests.get()));
                return Mono.empty();
            });

            return chain.filter(exchange)
                    .doFinally(signalType -> decrementConnections());
        }
    }
}