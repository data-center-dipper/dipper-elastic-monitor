package com.dipper.monitor.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class HttpContainer {

    @Value("${server.port:9750}")
    private Integer httpPort;

    /**
     * 是否开启 HTTP 服务
     */
    @Value("${dsource.http.flags:false}")
    private boolean httpFlags;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
        return factory -> {
            if (httpFlags) {
                factory.addAdditionalTomcatConnectors(createStandardConnector());
            }
        };
    }

    private Connector createStandardConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        return connector;
    }
}