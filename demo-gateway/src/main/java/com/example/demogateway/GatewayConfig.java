package com.example.demogateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.springframework.cloud.gateway.support.RouteMetadataUtils.CONNECT_TIMEOUT_ATTR;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR;

@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("notifications-route", predicateSpec -> predicateSpec
                        .path("/notifications")
                        .uri("http://localhost:5001/notifications")
                )


                .route("push-messages-route", r -> r
                        .path("/push_messages") 
                        .uri("http://localhost:5001/push_messages"))
                .build();
    }
}
