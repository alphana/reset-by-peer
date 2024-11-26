package com.example.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class Controller {
    private final Random random = new Random();

    @PostMapping("/notifications")
    public ResponseEntity<?> handleNotification() {
        log.info("handleNotification");
        return ResponseEntity.status(201).body("Success");
    }

    @PostMapping("/push_messages")
    public ResponseEntity<?> handlePushMessage() throws InterruptedException {
        // Simulate slow processing
        if (random.nextInt(100) < 30) {
            log.info("sleeping 10s");
            Thread.sleep(10000); // Sleep for 35 seconds
        }
        log.info("returning response");
        return ResponseEntity.status(201).body("Success");
    }

    @PostMapping("/push_messages1")
    public ResponseEntity<?> slowResponse(HttpServletRequest request, HttpServletResponse response) {
        log.debug("Received push message request");

        // 30% chance of problematic response
        if (random.nextInt(100) < 30) {
            log.debug("Triggering slow response scenario");
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Write response headers
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getOutputStream().write("{".getBytes());
                    response.getOutputStream().flush();

                    log.debug("Starting 33 second delay");
                    // Wait for 33 seconds (matching the production scenario)
                    Thread.sleep(33000);

                    log.debug("Forcing connection close");
                    // Force socket close to simulate connection reset
                    try {
                        Field outputStream = response.getClass().getDeclaredField("outputStream");
                        outputStream.setAccessible(true);
                        Object socketOutputStream = outputStream.get(response);

                        Field socket = socketOutputStream.getClass().getDeclaredField("socket");
                        socket.setAccessible(true);
                        Socket clientSocket = (Socket) socket.get(socketOutputStream);

                        clientSocket.setSoLinger(true, 0);
                        clientSocket.close();

                        log.debug("Connection forcibly closed");
                    } catch (Exception e) {
                        log.error("Error forcing connection close", e);
                    }
                } catch (Exception e) {
                    log.error("Error in async processing", e);
                }
            });

            try {
                future.get(35, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Error waiting for response", e);
            }
            return null; // Response already handled
        }

        log.debug("Sending normal response for push message");
        // Normal response for other cases
        return ResponseEntity.status(201).body("Created");
    }
}
