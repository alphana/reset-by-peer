package com.example.demogateway.fix;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("fixed")
@Slf4j
public class ResponseHandler implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();

        // Add response header handlers before response is committed
        response.beforeCommit(() -> {
            HttpHeaders headers = response.getHeaders();
            if (!headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
                // Ensure content-length is set if possible
                DataBufferFactory bufferFactory = response.bufferFactory();
            }
            return Mono.empty();
        });

        // Wrap the response to handle streaming responses
        return chain.filter(exchange)
                .transformDeferred(call -> {
                    // Add timeout for response completion
                    return call.timeout(Duration.ofSeconds(60))
                            .onErrorResume(TimeoutException.class, ex -> {
                                log.warn("Response timed out for request: {}",
                                        exchange.getRequest().getURI());
                                if (!response.isCommitted()) {
                                    response.setStatusCode(HttpStatus.GATEWAY_TIMEOUT);
                                    return response.setComplete();
                                }
                                return Mono.error(ex);
                            });
                })
                .doOnError(throwable -> {
                    if (!response.isCommitted()) {
                        log.error("Error occurred during response processing", throwable);
                        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        response.setComplete();
                    }
                })
                .doFinally(signalType -> {
                    if (signalType != SignalType.ON_COMPLETE && !response.isCommitted()) {
                        log.warn("Response not completed normally, signal: {}", signalType);
                        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                        response.setComplete();
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}