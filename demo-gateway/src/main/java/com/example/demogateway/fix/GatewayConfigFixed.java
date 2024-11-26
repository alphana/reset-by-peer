package com.example.demogateway.fix;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@Profile("fixed")
@Slf4j
public class GatewayConfigFixed {

    @Bean
    public HttpClient resilientHttpClient() {
        ConnectionProvider connectionProvider = getConnectionProvider();
        return createHttpClient(connectionProvider);
    }

    private ConnectionProvider getConnectionProvider() {
        return ConnectionProvider.builder("fixed")
                .maxConnections(500)
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .maxIdleTime(Duration.ofSeconds(60))
                .maxLifeTime(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .metrics(true)
                .build();
    }

    private HttpClient createHttpClient(ConnectionProvider connectionProvider) {
        return HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.TCP_KEEPIDLE, 300)
                .option(EpollChannelOption.TCP_KEEPINTVL, 60)
                .option(EpollChannelOption.TCP_KEEPCNT, 8)
                .option(ChannelOption.SO_LINGER, 0)  // Prevent TIME_WAIT state
                .option(ChannelOption.TCP_NODELAY, true)  // Disable Nagle's algorithm
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS))
                            .addHandlerLast(new IdleStateHandler(60, 60, 60, TimeUnit.SECONDS));
                    log.debug("Connection established with enhanced handlers: {}", conn);
                })
                .doOnChannelInit((observer, channel, address) -> {
                    channel.pipeline()
                            .addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                    if (evt instanceof IdleStateEvent) {
                                        log.debug("Channel idle, closing connection gracefully");
                                        ctx.close();
                                    }
                                    ctx.fireUserEventTriggered(evt);
                                }
                            });
                    log.debug("Channel initialized with idle state handler: {}", channel);
                })
                .doOnError(
                        (httpClientRequest, throwable) -> log.error("Connection error occurred in resilient client", throwable),
                        (httpClientResponse, throwable) -> log.error("Connection error occurred in resilient client", throwable)
                )
                .wiretap(true);
    }
}