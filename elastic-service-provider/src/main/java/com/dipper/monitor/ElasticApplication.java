package com.dipper.monitor;

import com.dipper.monitor.config.MyWebMvcConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

//import org.springframework.core.NestedIOException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;


@Slf4j
@ComponentScan(basePackages = {"com.dipper.monitor","com.xxl.job.plus"})
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class, MultipartAutoConfiguration.class})
@ServletComponentScan
@EnableScheduling // 开启定时任务支持
@Import(MyWebMvcConfig.class)
@MapperScan("com.dipper.monitor.mapper")
@SpringBootApplication
public class ElasticApplication {

    private static final Logger logger = LoggerFactory.getLogger(ElasticApplication.class);


    public static void main(String[] args) {
        // 设置自定义类加载器
        // 启动 Spring 应用
        ApplicationContext application = new SpringApplicationBuilder()
                .sources(ElasticApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);

        ClassLoader classLoader = application.getClassLoader();
        logger.info("当前类加载器:{}",classLoader);
        logger.info("当前父类加载器:{}",classLoader.getParent());

        logger.info("应用: {} 启动完成. ", application.getApplicationName());
    }

}