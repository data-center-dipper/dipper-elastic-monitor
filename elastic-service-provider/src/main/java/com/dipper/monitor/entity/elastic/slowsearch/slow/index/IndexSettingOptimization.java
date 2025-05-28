package com.dipper.monitor.entity.elastic.slowsearch.slow.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 索引设置优化建议
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexSettingOptimization {
    // 索引名称
    private String indexName;
    // 设置名称
    private String settingName;
    // 当前值
    private String currentValue;
    // 推荐值
    private String recommendedValue;
    // 描述
    private String description;
    // 影响级别
    private String impact;
}