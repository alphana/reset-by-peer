package com.example.demogateway;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import reactor.netty.http.client.HttpClient;

import java.time.Duration;


@Configuration
@Profile("faulty")
public class GatewayConfigFaulty {


//    @Bean
//    public HttpClient faultyHttpClient() {
//        return HttpClient.create()
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
////                .option(ChannelOption.SO_KEEPALIVE, true)
////                // Netty native transport options for keepalive
////                .option(EpollChannelOption.TCP_KEEPIDLE, 300)
////                .option(EpollChannelOption.TCP_KEEPINTVL, 60)
////                .option(EpollChannelOption.TCP_KEEPCNT, 8)
//                .responseTimeout(Duration.ofSeconds(60))  // Set slightly higher than observed timeout
//                ;
//    }


}