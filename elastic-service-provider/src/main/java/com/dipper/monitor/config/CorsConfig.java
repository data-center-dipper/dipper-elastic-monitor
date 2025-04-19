package com.dipper.monitor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Slf4j
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        log.info("跨域相关配置。。。");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 允许cookie等凭证随请求一起发送

        // 明确列出所有允许的源地址
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://127.0.0.1:9750");
        config.addAllowedOrigin("http://192.168.56.33:9750");
        config.addAllowedOrigin("http://doris04:9750");
        config.addAllowedOrigin("http://dipper.monitor.com:9750");
        config.addAllowedOrigin("http://82167a88006e1e15.natapp.cc");
        config.addAllowedOrigin("http://natapp.cc");

        config.addAllowedHeader("*"); // 允许所有头
        config.addAllowedMethod("*"); // 允许所有方法
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}