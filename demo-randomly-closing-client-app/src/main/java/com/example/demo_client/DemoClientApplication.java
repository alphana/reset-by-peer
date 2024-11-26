package com.example.demo_client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
@SpringBootApplication
public class DemoClientApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoClientApplication.class, args);
    }


    @Override
    public void run(String... args) {
        // Start a scheduled task to simulate random connection closures
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::makeRequest, 0, 2, TimeUnit.SECONDS);
    }

    private void makeRequest() {
        Random random = new Random();
        boolean closeAbruptly = random.nextBoolean(); // Randomly decide whether to close the connection abruptly

        // Make URL configurable
        String gatewayUrl = "http://localhost:8991";
        if (closeAbruptly) {
            // Simulate abrupt closure by starting a connection and not waiting for its completion
            System.out.println("Connecting and closing abruptly without sending data.");
            try {
                WebClient.create(gatewayUrl)
                        .post()
                        .uri("/notifications")
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribe(
                                response -> System.out.println("Should not reach here: " + response),
                                error -> System.out.println("Simulated abrupt connection closed."));

                WebClient.create(gatewayUrl)
                        .post()
                        .uri("/push_messages")
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribe(
                                response -> System.out.println("Should not reach here: " + response),
                                error -> System.out.println("Simulated abrupt connection closed."));

//                push_messages
                // No further action: let the connection terminate unexpectedly
            } catch (Exception e) {
                System.err.println("Error during abrupt closure: " + e.getMessage());
            }
        } else {
            // Send a proper REST call and handle normally
            System.out.println("Sending normal REST call to gateway.");
            WebClient.create(gatewayUrl)
                    .post()
                    .uri("/notifications")
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(response -> System.out.println("Response received: " + response))
                    .doOnError(error -> System.err.println("Error during normal request: " + error.getMessage()))
                    .timeout(Duration.ofSeconds(5))
                    .subscribe();
            WebClient.create(gatewayUrl)
                    .post()
                    .uri("/push_messages")
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(response -> System.out.println("Response received: " + response))
                    .doOnError(error -> System.err.println("Error during normal request: " + error.getMessage()))
                    .timeout(Duration.ofSeconds(5))
                    .subscribe();
        }
    }

}
