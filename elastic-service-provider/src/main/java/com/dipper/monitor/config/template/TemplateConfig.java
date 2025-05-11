package com.dipper.monitor.config.template;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class TemplateConfig {
    /**
     * 预制模版路径
     */
    @Value("${prefabricate.template.path}")
    private String prefabricateTemplatePath;
}
