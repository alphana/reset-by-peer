package com.example.demogateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.cloud.gateway.support.RouteMetadataUtils.CONNECT_TIMEOUT_ATTR;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR;

@Configuration
@Slf4j
public class GatewayConfig {
//    public static final String BACKEND_URL = "http://localhost:5002";
    public static final String BACKEND_URL = "http://localhost:5001";

    //    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//
//                .route("notifications-route", predicateSpec -> predicateSpec
//                        .path("/notifications")
//                        .uri("http://localhost:5001/notifications")
//                )
//
//
//                .route("push-messages-route", r -> r
//                        .path("/push_messages")
//                        .uri("http://localhost:5001/push_messages"))
//                .build();
//    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("api-route", r -> r
                        .path("/{api-name}/**")
                        .filters(f -> f
                                .rewritePath("/(?<apiName>[^/]+)/(?<segment>.*)",
                                        "/${apiName}WebApiV2/${segment}")
                                .modifyResponseBody(String.class, String.class, (exchange, s) -> {
                                    String apiName = exchange.getRequest().getURI().getPath().split("/")[1];
                                    log.debug("[{}] Response body: {}", apiName, s);
                                    return Mono.justOrEmpty(s);
                                })
                                .addResponseHeader("X-Response-Time",
                                        String.valueOf(System.currentTimeMillis()))
                                .filter((exchange, chain) -> {
                                    String apiName = exchange.getRequest().getURI().getPath().split("/")[1];
                                    log.debug("Processing request for API: {}", apiName);
                                    ServerHttpRequest request = exchange.getRequest();
                                    log.debug("[{}] {} {}", apiName, request.getMethod(), request.getURI());
                                    return chain.filter(exchange);
                                }))
                        .uri(BACKEND_URL))
                .build();
    }
}
