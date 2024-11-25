package com.example.democlientapp.ClientService;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@SpringBootApplication
@EnableScheduling
public class DemoClientAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoClientAppApplication.class, args);
    }


}
