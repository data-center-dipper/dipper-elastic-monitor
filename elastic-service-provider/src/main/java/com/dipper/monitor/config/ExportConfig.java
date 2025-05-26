package com.dipper.monitor.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ExportConfig {

    @Value("${export.base.path:E:\\export}")
    private String exportBasePath;


    @Value("${import.base.path:E:\\import}")
    private String importBasePath;
}
