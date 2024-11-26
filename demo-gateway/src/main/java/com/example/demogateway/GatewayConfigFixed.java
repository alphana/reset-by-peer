package com.example.demogateway;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Profile("fix")
public class GatewayConfigFixed {


    @Bean
    public HttpClient httpClient() {
        String poolName = "fixed";

        // Create connection provider with proper settings
        ConnectionProvider provider = ConnectionProvider.builder(poolName)
                .maxConnections(500)  // Increased from previous small pool
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .metrics(true)
                .build();

        // Create and warmup the HttpClient with proper timeouts
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(30))  // Add response timeout
                .doOnConnected(conn -> {
                    log.info("New connection established: {}", conn.channel().id());
                    conn.addHandlerLast(new LoggingHandler("reactor.netty.http.client", LogLevel.DEBUG));
                })
                .doOnDisconnected(conn ->
                        log.info("Connection disconnected: {}", conn.channel().id())
                );

        log.info("Warming up HttpClient...");
        httpClient.warmup().block();
        log.info("HttpClient warmup completed");

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