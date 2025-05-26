package com.dipper.monitor.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SlowSearchConfig {

    // 数据保留天数，默认7天
    @Value("${elastic.monitor.slow-query.retention-days:7}")
    private int retentionDays;

    // 慢查询阈值，默认1000ms
    @Value("${elastic.monitor.slow-query.threshold:1000}")
    private int slowQueryThreshold;
}
