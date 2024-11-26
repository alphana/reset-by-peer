package com.example.democlientapp.ClientService;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
@Log4j2
public class ClientService {

    @Value("${gateway.url:http://localhost:8991}")
    private String gatewayUrl;  // Make URL configurable

    private final RestTemplate restTemplate;

    public ClientService(RestTemplate restTemplate) {  // Inject RestTemplate
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    void postInit(){
        log.info("service init done");
    }

    @Scheduled(fixedRate = 2000)
    public void sendRequests() {
        try {
            // Send normal notification
            log.info("/notifications");
            restTemplate.postForEntity(
                    gatewayUrl + "/notifications",
                    createRequest(),
                    String.class
            );

            // Send push message that will trigger timeout
            log.info("/push_messages");
            restTemplate.postForEntity(
                    gatewayUrl + "/push_messages",
                    createRequest(),
                    String.class
            );
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private HttpEntity<?> createRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-consumer", "{\"applicationContextV1\": {\"lang\": \"tr\", \"consumerCode\": \"APP.PFM\", \"channelCode\":\"application\", \"ipAddress\": \"10.66.28.64\"}}");
        return new HttpEntity<>(new HashMap<>(), headers);
    }


}
