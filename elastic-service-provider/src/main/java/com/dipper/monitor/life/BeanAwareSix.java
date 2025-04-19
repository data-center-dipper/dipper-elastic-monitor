package com.dipper.monitor.life;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringValueResolver;


@Slf4j
public class BeanAwareSix implements EnvironmentAware, EmbeddedValueResolverAware, ResourceLoaderAware, ApplicationEventPublisherAware, MessageSourceAware, ApplicationContextAware {

    @PostConstruct
    public void postConstruct1() { //@1
        log.info("postConstruct1()");
    }

    @PostConstruct
    public void postConstruct2() { //@2
        log.info("postConstruct2()");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("setApplicationContext:" + applicationContext);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        log.info("setApplicationEventPublisher:" + applicationEventPublisher);
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        log.info("setEmbeddedValueResolver:" + resolver);
    }

    @Override
    public void setEnvironment(Environment environment) {
        log.info("setEnvironment:" + environment.getClass());
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        log.info("setMessageSource:" + messageSource);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        log.info("setResourceLoader:" + resourceLoader);
    }
}
