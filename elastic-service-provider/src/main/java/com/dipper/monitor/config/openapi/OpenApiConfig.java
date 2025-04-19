package com.dipper.monitor.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Configuration
public class OpenApiConfig {

    private final Environment environment;

    @Value("${springdoc.api-docs.version:OPENAPI_3_0}")
    private String version;

    @Value("${server.port:9750}")
    private String port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public OpenApiConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * 配置全局的OpenAPI信息。
     *
     * 【Spring】Spring使用springdoc-openapi入门案例，以及怎么实现分组展示
     * https://blog.csdn.net/qq_21383435/article/details/144934770
     */
    @Bean
    public OpenAPI springOpenAPI() {
        // 创建一个OpenAPI对象并设置基本信息、安全方案和服务器信息
        OpenAPI openAPI = new OpenAPI()
                .info(new Info().title("七星监控").description("七星监控的接口信息").version(version))
                .components(new Components()
                        // 添加全局的安全方案定义，使用Bearer Token进行身份验证
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // 设置默认的服务器URL和描述
                .addServersItem(new Server()
                        .url(String.format("http://%s:%s%s", "localhost", port, contextPath))
                        .description("本地开发环境"));

        // 添加标签到API文档，用于组织API分组
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag().name("测试TagA"));
        tags.add(new Tag().name("测试TagB"));
        openAPI.setTags(tags);

        // 打印服务器信息、openapi版本和接口访问地址的日志信息
        log.info("server信息:{}", openAPI.getServers());
        log.info("openapi=>{}", openAPI.getOpenapi());
        log.info("接口访问地址:http://localhost:" + port + contextPath + "/swagger-ui/index.html");

        return openAPI;
    }

    /**
     * 创建Kafka Monitor分组的API文档。
     */
    @Bean
    public GroupedOpenApi kafkaApis() {
        // 指定要扫描的包路径，以确定哪些API应该包含在这个分组中
        String[] packagesToScan = {"com.dipper.monitor.controller.elsatic"};
        return GroupedOpenApi.builder()
                .group("elsatic-monitor") // 分组名称
                .packagesToScan(packagesToScan) // 要扫描的包
                .pathsToMatch("/dipper/monitor/api/v1/elsatic/**") // 可选：进一步限制路径匹配规则
                .build();
    }

    /**
     * 创建Elasticsearch Monitor分组的API文档。
     */
    @Bean
    public GroupedOpenApi elasticsearchApis() {
        // 指定要扫描的包路径，以确定哪些API应该包含在这个分组中
        String[] packagesToScan = {"com.dipper.monitor.controller.elastic"};
        return GroupedOpenApi.builder()
                .group("elasticsearch-monitor") // 分组名称
                .packagesToScan(packagesToScan) // 要扫描的包
                .pathsToMatch("/dipper/monitor/api/v1/elastic/**") // 可选：进一步限制路径匹配规则
                .build();
    }
}