package com.example.demogateway;

import io.netty.channel.ChannelOption;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
@Profile("faulty")
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("notifications-route", r -> r
                        .path("/notifications")
                        .uri("http://localhost:5001/notifications"))
                .route("push-messages-route", r -> r
                        .path("/push_messages")
                        .uri("http://localhost:5001/push_messages"))
                .build();
    }

    @Bean
    public HttpClient httpClient() {
        // Create connection provider with the same settings
        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        // Create and warmup the HttpClient
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        httpClient.warmup().block();

        return httpClient;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(HttpClient httpClient, WebClient.Builder builder) {
        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}