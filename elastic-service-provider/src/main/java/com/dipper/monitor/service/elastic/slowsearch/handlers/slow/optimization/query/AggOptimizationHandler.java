package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.query;

import com.alibaba.fastjson.JSONObject;

public class AggOptimizationHandler extends AbstractOptimizationHandler {
    
    public AggOptimizationHandler(JSONObject setting, JSONObject mappings) {
        super(setting, mappings);
    }
    
    public void generateAggregationQueryOptimization(JSONObject result, JSONObject mappings) {
        result.put("issueType", "聚合查询性能问题");
        result.put("impact", "中");
        result.put("optimizedTime", 3100); // 预计优化后时间

        StringBuilder suggestion = new StringBuilder();
        suggestion.append("1. 减少聚合桶的数量\n")
                .append("2. 使用更精确的过滤条件减少处理文档数\n")
                .append("3. 考虑使用预计算或缓存聚合结果");

        if (mappings != null) {
            JSONObject properties = mappings.getJSONObject("mappings").getJSONObject("properties");
            for (String key : properties.keySet()) {
                JSONObject field = properties.getJSONObject(key);
                if ("text".equals(field.getString("type"))) {
                    suggestion.append("\n4. 聚合字段 ").append(key).append(" 为 text 类型，建议使用 .keyword 后缀以提升性能");
                }
            }
        }

        result.put("suggestion", suggestion.toString());

        // 示例优化查询
        JSONObject optimizedQuery = new JSONObject();
        JSONObject aggs = new JSONObject();
        JSONObject terms = new JSONObject();
        terms.put("field", "category.keyword");
        terms.put("size", 10);
        aggs.put("category_terms", terms);
        optimizedQuery.put("aggs", aggs);

        JSONObject query = new JSONObject();
        JSONObject bool = new JSONObject();
        JSONObject filter = new JSONObject();
        JSONObject range = new JSONObject();
        JSONObject timestamp = new JSONObject();
        timestamp.put("gte", "now-1d");
        range.put("timestamp", timestamp);
        filter.put("range", range);
        bool.put("filter", filter);
        query.put("bool", bool);
        optimizedQuery.put("query", query);

        result.put("optimizedQuery", optimizedQuery);
        result.put("explanation", "聚合查询在处理大量数据时可能会消耗大量内存和CPU资源。通过限制聚合桶的数量和添加时间范围过滤器，可以减少需要处理的数据量，从而提高查询性能。对于频繁执行的聚合查询，可以考虑使用预计算或缓存结果。");
    }
}
