package com.dipper.monitor.config;

import cn.hutool.core.io.FileUtil;
import com.dipper.common.lib.utils.ApplicationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Configuration
public class MyWebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(MyWebMvcConfig.class);


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        logger.info("Adding MappingJackson2HttpMessageConverter to message converters.");
        converters.add(new MappingJackson2HttpMessageConverter());
    }

    @Bean
    public ServletListenerRegistrationBean<?> configContextListener() {
        logger.info("Registering ConfigContextListener");
        ServletListenerRegistrationBean<ConfigContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new ConfigContextListener());
        return bean;
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 处理根路径和非静态资源路径
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    /**
     * 这一点想实现，前端其他界面豆奶访问，但是没有实现，属于无效代码，目前看起来没有影响
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return requestedResource.exists() && requestedResource.isReadable() ? requestedResource : location.createRelative("index.html");
                    }
                });
    }
}