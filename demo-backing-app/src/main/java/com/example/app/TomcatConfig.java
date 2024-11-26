package com.example.app;


import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

class TomcatConfig {

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);

                // Set connection timeout to match the scenario
                connector.setProperty("connectionTimeout", "35000");
                connector.setProperty("keepAliveTimeout", "35000");

                // Enable socket options that allow us to force connection resets
                connector.setProperty("socket.soLinger", "0");
                connector.setProperty("socket.soKeepAlive", "true");
            }
        };
    }
}