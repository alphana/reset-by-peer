package com.example.demobackingapp;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class Controller {
    @PostMapping("/notifications")
    public ResponseEntity<?> handleNotification() {
        log.info("handleNotification");
        return ResponseEntity.status(201).body("Success");
    }

    @PostMapping("/push_messages")
    public ResponseEntity<?> handlePushMessage() throws InterruptedException {
        // Simulate slow processing
        log.info("sleeping 35s");
        Thread.sleep(35000); // Sleep for 35 seconds
        log.info("returning response");
        return ResponseEntity.status(201).body("Success");
    }
}
