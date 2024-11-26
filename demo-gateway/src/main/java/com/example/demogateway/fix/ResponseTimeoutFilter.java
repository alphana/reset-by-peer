package com.example.demogateway.fix;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import java.util.concurrent.TimeoutException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("fixed")
@Slf4j
public class ResponseTimeoutFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();

        return chain.filter(exchange)
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(TimeoutException.class, ex -> {
                    log.warn("Request timed out: {}", exchange.getRequest().getURI());
                    response.setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
                    return response.setComplete();
                })
                .doOnError(throwable -> {
                    if (!response.isCommitted()) {
                        log.error("Error occurred during request processing", throwable);
                        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        response.setComplete();
                    }
                })
                .doFinally(signalType -> {
                    if (signalType == SignalType.CANCEL && !response.isCommitted()) {
                        log.warn("Request cancelled, completing response");
                        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                        response.setComplete();
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}