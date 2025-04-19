package com.dipper.monitor.config.listeners;

import com.dipper.monitor.config.plugins.PluginsConfigUtils;
import com.dipper.monitor.utils.plugins.PluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;

/***
 * todo: 九师兄  2024/3/25 19:34
 * Spring Boot : SpringApplicationRunListener
 * 原文链接：https://blog.csdn.net/qq_21383435/article/details/105034794
 */
public class StartSpringApplicationRunListeners implements SpringApplicationRunListener {

    private static final Logger logger = LoggerFactory.getLogger(StartSpringApplicationRunListeners.class);

    public StartSpringApplicationRunListeners(SpringApplication springApplication, String[] args) {
        try {
            logger.info("StartSpringApplicationRunListener 初始化");
        }catch (Exception e){
            logger.error("StartSpringApplicationRunListener 初始化失败",e);
        }
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        SpringApplicationRunListener.super.starting(bootstrapContext);
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        SpringApplicationRunListener.super.environmentPrepared(bootstrapContext, environment);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        SpringApplicationRunListener.super.contextPrepared(context);
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        SpringApplicationRunListener.super.contextLoaded(context);
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        SpringApplicationRunListener.super.started(context, timeTaken);
    }


    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
        SpringApplicationRunListener.super.ready(context, timeTaken);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        SpringApplicationRunListener.super.failed(context, exception);
    }
}
