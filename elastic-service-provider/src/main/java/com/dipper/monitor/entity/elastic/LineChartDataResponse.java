package com.dipper.monitor.entity.elastic;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 折线图数据响应实体类
 */
@Data
public class LineChartDataResponse {
    private List<Map<String, Object>> gcData;
    private List<Map<String, Object>> networkData;
    private List<Map<String, Object>> memoryData;
}