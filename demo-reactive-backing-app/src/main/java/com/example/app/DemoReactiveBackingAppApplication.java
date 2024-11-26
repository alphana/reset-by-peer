package com.example.app;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@SpringBootApplication
public class DemoReactiveBackingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoReactiveBackingAppApplication.class, args);
    }

}


